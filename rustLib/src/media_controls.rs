//! System media controls: SMTC on Windows, Now Playing on macOS, MPRIS on Linux.

use std::fmt::Debug;
use std::sync::mpsc;
use std::sync::Arc;
use std::thread;
use std::time::Duration;

use log::{info, warn};
use souvlaki::{MediaMetadata, MediaPlayback, MediaPosition, PlatformConfig, SeekDirection};

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum MediaControlsError {
    #[error("Failed to initialize media controls: {msg}")]
    InitError { msg: String },
}

/// Commands sent by the system, e.g. media keys or the OS media widget.
#[derive(uniffi::Enum, Debug)]
pub enum MediaControlEvent {
    Play,
    Pause,
    Toggle,
    Next,
    Previous,
    Stop,
    /// Seek relative to the current position. `offset` is `None` when the system does not specify one.
    SeekBy { forward: bool, offset: Option<Duration> },
    SetPosition { position: Duration },
    /// Bring the app window to the front.
    Raise,
}

#[uniffi::export(with_foreign)]
pub trait MediaControlsListener: Send + Sync + Debug {
    fn on_event(&self, event: MediaControlEvent);
}

#[derive(uniffi::Record, Debug)]
pub struct MediaInfo {
    pub title: String,
    pub artist: String,
    /// Absolute path of a local cover image file.
    pub cover_path: Option<String>,
    pub duration: Option<Duration>,
}

#[derive(uniffi::Enum, Debug)]
pub enum PlaybackState {
    Playing { position: Duration },
    Paused { position: Duration },
    Stopped,
}

enum Command {
    SetMetadata(Option<MediaInfo>),
    SetPlayback(PlaybackState),
}

/// All platform calls run on a dedicated thread. On Windows that thread owns the hidden window SMTC is bound to,
/// so it must keep pumping window messages.
#[derive(uniffi::Object)]
pub struct MediaControls {
    tx: mpsc::Sender<Command>,
}

#[uniffi::export]
impl MediaControls {
    /// `dbus_name` becomes `org.mpris.MediaPlayer2.<dbus_name>` on Linux.
    #[uniffi::constructor]
    pub fn new(
        display_name: String,
        dbus_name: String,
        listener: Arc<dyn MediaControlsListener>,
    ) -> Result<Arc<Self>, MediaControlsError> {
        let (tx, rx) = mpsc::channel();
        let (ready_tx, ready_rx) = mpsc::channel();
        thread::Builder::new()
            .name("media-controls".to_string())
            .spawn(move || run(display_name, dbus_name, listener, rx, ready_tx))
            .map_err(|e| MediaControlsError::InitError { msg: e.to_string() })?;
        ready_rx.recv().map_err(|e| MediaControlsError::InitError { msg: e.to_string() })??;
        Ok(Arc::new(Self { tx }))
    }

    pub fn set_metadata(&self, info: Option<MediaInfo>) {
        _ = self.tx.send(Command::SetMetadata(info));
    }

    pub fn set_playback(&self, state: PlaybackState) {
        _ = self.tx.send(Command::SetPlayback(state));
    }
}

fn run(
    display_name: String,
    dbus_name: String,
    listener: Arc<dyn MediaControlsListener>,
    rx: mpsc::Receiver<Command>,
    ready_tx: mpsc::Sender<Result<(), MediaControlsError>>,
) {
    let init_error = |msg: String| MediaControlsError::InitError { msg };

    #[cfg(target_os = "windows")]
    let window = match windows_window::HiddenWindow::new() {
        Ok(window) => window,
        Err(e) => {
            _ = ready_tx.send(Err(init_error(e)));
            return;
        }
    };
    #[cfg(target_os = "windows")]
    let hwnd = Some(window.hwnd());
    #[cfg(not(target_os = "windows"))]
    let hwnd = None;

    let config = PlatformConfig {
        display_name: &display_name,
        dbus_name: &dbus_name,
        hwnd,
    };
    let controls = match souvlaki::MediaControls::new(config) {
        Ok(controls) => controls,
        Err(e) => {
            _ = ready_tx.send(Err(init_error(format!("{:?}", e))));
            return;
        }
    };
    let handler = move |event| {
        if let Some(event) = convert_event(event) {
            listener.on_event(event);
        }
    };

    // MPRemoteCommandCenter only publishes the enabled commands to Control Center when used on the main thread.
    // Off the main thread, media keys still work but Control Center greys out next/previous and seeking.
    #[cfg(target_os = "macos")]
    let controls = {
        let controls = Arc::new(std::sync::Mutex::new(controls));
        let main_controls = Arc::clone(&controls);
        dispatch::Queue::main().exec_async(move || {
            if let Err(e) = main_controls.lock().unwrap().attach(handler) {
                warn!("Failed to attach media controls: {:?}", e);
            }
        });
        controls
    };
    #[cfg(not(target_os = "macos"))]
    let mut controls = controls;
    #[cfg(not(target_os = "macos"))]
    {
        if let Err(e) = controls.attach(handler) {
            _ = ready_tx.send(Err(init_error(format!("{:?}", e))));
            return;
        }
    }
    _ = ready_tx.send(Ok(()));
    info!("Media controls initialized");

    loop {
        #[cfg(target_os = "windows")]
        let command = {
            window.pump_messages();
            match rx.recv_timeout(Duration::from_millis(50)) {
                Ok(command) => command,
                Err(mpsc::RecvTimeoutError::Timeout) => continue,
                Err(mpsc::RecvTimeoutError::Disconnected) => break,
            }
        };
        #[cfg(not(target_os = "windows"))]
        let command = match rx.recv() {
            Ok(command) => command,
            Err(_) => break,
        };

        #[cfg(target_os = "macos")]
        {
            let controls = Arc::clone(&controls);
            dispatch::Queue::main().exec_async(move || apply(&mut controls.lock().unwrap(), command));
        }
        #[cfg(not(target_os = "macos"))]
        {
            apply(&mut controls, command);
        }
    }

    // The handle was dropped on the Kotlin side; dropping `controls` detaches them
    #[cfg(target_os = "macos")]
    dispatch::Queue::main().exec_async(move || drop(controls));
    info!("Media controls released");
}

fn apply(controls: &mut souvlaki::MediaControls, command: Command) {
    let result = match command {
        Command::SetMetadata(info) => {
            let cover_url = info.as_ref().and_then(|it| it.cover_path.as_deref()).and_then(cover_url);
            let metadata = match &info {
                Some(info) => MediaMetadata {
                    title: Some(&info.title),
                    artist: Some(&info.artist),
                    album: None,
                    cover_url: cover_url.as_deref(),
                    duration: info.duration,
                },
                None => MediaMetadata::default(),
            };
            controls.set_metadata(metadata)
        }
        Command::SetPlayback(state) => controls.set_playback(match state {
            PlaybackState::Playing { position } => MediaPlayback::Playing {
                progress: Some(MediaPosition(position)),
            },
            PlaybackState::Paused { position } => MediaPlayback::Paused {
                progress: Some(MediaPosition(position)),
            },
            PlaybackState::Stopped => MediaPlayback::Stopped,
        }),
    };
    if let Err(e) = result {
        warn!("Failed to update media controls: {:?}", e);
    }
}

fn convert_event(event: souvlaki::MediaControlEvent) -> Option<MediaControlEvent> {
    use souvlaki::MediaControlEvent as E;
    Some(match event {
        E::Play => MediaControlEvent::Play,
        E::Pause => MediaControlEvent::Pause,
        E::Toggle => MediaControlEvent::Toggle,
        E::Next => MediaControlEvent::Next,
        E::Previous => MediaControlEvent::Previous,
        E::Stop => MediaControlEvent::Stop,
        E::Seek(direction) => MediaControlEvent::SeekBy {
            forward: direction == SeekDirection::Forward,
            offset: None,
        },
        E::SeekBy(direction, offset) => MediaControlEvent::SeekBy {
            forward: direction == SeekDirection::Forward,
            offset: Some(offset),
        },
        E::SetPosition(MediaPosition(position)) => MediaControlEvent::SetPosition { position },
        E::Raise => MediaControlEvent::Raise,
        E::SetVolume(_) | E::OpenUri(_) | E::Quit => return None,
    })
}

/// Each backend reads the cover URL differently, see [MediaMetadata::cover_url].
fn cover_url(path: &str) -> Option<String> {
    if cfg!(target_os = "windows") {
        // souvlaki strips the `file://` prefix and opens the rest as a Windows path
        Some(format!("file://{}", path))
    } else {
        url::Url::from_file_path(path).ok().map(|it| it.to_string())
    }
}

#[cfg(target_os = "windows")]
mod windows_window {
    use std::mem;

    use windows::core::PCWSTR;
    use windows::w;
    use windows::Win32::Foundation::{HWND, LPARAM, LRESULT, WPARAM};
    use windows::Win32::System::LibraryLoader::GetModuleHandleW;
    use windows::Win32::UI::WindowsAndMessaging::{
        CreateWindowExW, DefWindowProcW, DestroyWindow, DispatchMessageW, PeekMessageW, RegisterClassExW,
        TranslateMessage, MSG, PM_REMOVE, WINDOW_EX_STYLE, WINDOW_STYLE, WNDCLASSEXW,
    };

    /// SMTC has to be bound to a window. The app window is destroyed when minimized to the tray,
    /// so the controls get a hidden window of their own.
    pub struct HiddenWindow {
        handle: HWND,
    }

    impl HiddenWindow {
        pub fn new() -> Result<HiddenWindow, String> {
            let class_name = w!("HachimiWorldMediaControls");
            unsafe {
                let instance = GetModuleHandleW(None).map_err(|e| format!("Getting module handle failed: {e}"))?;
                let wnd_class = WNDCLASSEXW {
                    cbSize: mem::size_of::<WNDCLASSEXW>() as u32,
                    hInstance: instance,
                    lpszClassName: PCWSTR::from(class_name),
                    lpfnWndProc: Some(Self::wnd_proc),
                    ..Default::default()
                };
                // Fails when the class is already registered by a previous instance, which is fine
                RegisterClassExW(&wnd_class);

                let handle = CreateWindowExW(
                    WINDOW_EX_STYLE::default(),
                    class_name,
                    w!(""),
                    WINDOW_STYLE::default(),
                    0,
                    0,
                    0,
                    0,
                    None,
                    None,
                    instance,
                    None,
                );
                if handle.0 == 0 {
                    Err(format!("Window creation failed: {}", std::io::Error::last_os_error()))
                } else {
                    Ok(HiddenWindow { handle })
                }
            }
        }

        pub fn hwnd(&self) -> *mut std::ffi::c_void {
            self.handle.0 as _
        }

        /// Must run regularly on the thread that created the window, so broadcast messages are not left waiting.
        pub fn pump_messages(&self) {
            unsafe {
                let mut msg: MSG = mem::zeroed();
                while PeekMessageW(&mut msg, None, 0, 0, PM_REMOVE).as_bool() {
                    TranslateMessage(&msg);
                    DispatchMessageW(&msg);
                }
            }
        }

        extern "system" fn wnd_proc(hwnd: HWND, msg: u32, wparam: WPARAM, lparam: LPARAM) -> LRESULT {
            unsafe { DefWindowProcW(hwnd, msg, wparam, lparam) }
        }
    }

    impl Drop for HiddenWindow {
        fn drop(&mut self) {
            unsafe {
                DestroyWindow(self.handle);
            }
        }
    }
}
