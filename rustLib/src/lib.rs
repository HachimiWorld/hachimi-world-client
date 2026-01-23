use crate::PlayerError::{IOError, RodioError, URLError};
use rodio::source::SeekError;
use rodio::{Decoder, OutputStreamBuilder, Sink, Source};
use std::fmt::Debug;
use std::fs::File;
use std::sync::mpsc::Sender;
use std::sync::{mpsc, Arc, Mutex, RwLock};
use std::thread;
use std::thread::JoinHandle;
use std::time::Duration;
use log::{debug, info, warn};
use stream_download::http::{HttpStream};
use stream_download::source::SourceStream;
use stream_download::storage::temp::TempStorageProvider;
use stream_download::{Settings, StreamDownload, StreamPhase, StreamState};
use url::Url;

uniffi::setup_scaffolding!("hachimi");

#[uniffi::export]
fn init() {
    println!("Rust library initialized");
}

static VM: once_cell::sync::OnceCell<jni::JavaVM> = once_cell::sync::OnceCell::new();

#[unsafe(export_name = "Java_world_hachimi_app_jni_JniLoader_javaInit")]
pub extern "system" fn java_init(env: jni::JNIEnv, _obj: jni::objects::JObject) {
    let vm = env.get_java_vm().unwrap();
    _ = VM.set(vm);
}

#[derive(Clone, uniffi::Object)]
pub struct Player {
    _player_thread: Arc<JoinHandle<()>>,
    ctrl_tx: Sender<PlayerControl>,
    current_item: Arc<RwLock<Option<MediaItem>>>,
    stream_play: Arc<RwLock<bool>>,
    buffer_progress: Arc<RwLock<f32>>,
    content_length: Arc<RwLock<Option<u64>>>,
    event_listener: Arc<RwLock<Option<Arc<dyn PlayerEventListener>>>>,
    stop_by_user: Arc<Mutex<bool>>,
    event_tx: Sender<PlayerEvent>,
    rt: Arc<tokio::runtime::Runtime>,
}

#[derive(uniffi::Record, PartialEq, Clone)]
pub struct MediaItem {
    pub audio_url: String,
    pub format: String,
    pub replay_gain_db: f32,
    pub duration_secs: i32
}

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum PlayerError {
    #[error("Invalid url: {msg}")]
    URLError { msg: String },
    #[error("IO error: {msg}")]
    IOError { msg: String },
    #[error("Error occurred in rodio: {msg}")]
    RodioError { msg: String },
}

#[derive(uniffi::Enum)]
pub enum PlayerEvent {
    Play, Pause, Seek(Duration), Stop, End
}

#[uniffi::export(with_foreign)]
pub trait PlayerEventListener : Send + Sync + Debug {
    fn on_event(&self, event: PlayerEvent);
}

pub enum PlayerControl {
    Play, Pause, Stop(oneshot::Sender<()>), Seek(Duration, oneshot::Sender<Result<(), PlayerError>>),
    Append(Box<dyn Source + Sync + Send>),
    SetVolume(f32),
    GetVolume(oneshot::Sender<f32>),
    GetPos(oneshot::Sender<Duration>),
    IsPaused(oneshot::Sender<bool>),
    IsEmpty(oneshot::Sender<bool>),
    Drain(oneshot::Sender<()>),
}

#[uniffi::export]
impl Player {
    #[uniffi::constructor]
    pub fn new() -> Player {
        let runtime = tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .on_thread_start(|| {
                if let Some(vm) = VM.get() {
                    vm.attach_current_thread_permanently().unwrap();
                } else {
                    warn!("Java VM is not initialized")
                }
            })
            .build()
            .unwrap();

        let (event_tx, event_rx) = mpsc::channel::<PlayerEvent>();
        let event_listener = Arc::new(RwLock::new(None));

        let (ctrl_tx, ctrl_rx) = mpsc::channel::<PlayerControl>();

        let stop_by_user = Arc::new(Mutex::new(false));
        // Player thread, this is because the `OutputStream` is !Send + !Sync
        let player_thread = thread::spawn({
            let event_tx = event_tx.clone();
            let stop_by_user = Arc::clone(&stop_by_user);
            move || {
                let stream_handler =
                    OutputStreamBuilder::open_default_stream().expect("Failed to open default stream");

                let sink = Arc::new(Sink::connect_new(&stream_handler.mixer()));

                for msg in ctrl_rx {
                    match msg {
                        PlayerControl::Play => {
                            sink.play();
                            event_tx.send(PlayerEvent::Play).unwrap();
                        }
                        PlayerControl::Pause => {
                            sink.pause();
                            event_tx.send(PlayerEvent::Pause).unwrap();
                        }
                        PlayerControl::Stop(tx) => {
                            sink.stop();
                            tx.send(()).unwrap();
                            event_tx.send(PlayerEvent::Stop).unwrap();
                        },
                        PlayerControl::Append(source) => {
                            sink.append(source);
                            sink.pause();
                            thread::spawn({
                                let sink = Arc::clone(&sink);
                                let event_tx = event_tx.clone();
                                let stop_by_user = Arc::clone(&stop_by_user);
                                move || {
                                    sink.sleep_until_end();
                                    let guard = stop_by_user.lock().unwrap();
                                    if *guard == false {
                                        event_tx.send(PlayerEvent::End).unwrap();
                                    }
                                }
                            });
                        },
                        PlayerControl::Seek(pos, callback) => {
                            let r = sink.try_seek(pos).map_err(|x| match x {
                                SeekError::NotSupported { underlying_source } => RodioError {
                                    msg: format!("Not supported: {}", underlying_source),
                                },
                                SeekError::SymphoniaDecoder(e) => RodioError {
                                    msg: format!("Symphonia decoder returned an error: {:?}", e),
                                },
                                SeekError::Other(e) => RodioError {
                                    msg: format!("Other error: {:?}", e),
                                },
                                _ => RodioError {
                                    msg: format!("Unexpected error: {:?}", x),
                                },
                            });
                            callback.send(r).unwrap();
                            event_tx.send(PlayerEvent::Seek(pos)).unwrap();
                        },
                        PlayerControl::SetVolume(volume) => {
                            sink.set_volume(volume);
                        }
                        PlayerControl::GetVolume(tx) => {
                            tx.send(sink.volume()).unwrap();
                        }
                        PlayerControl::GetPos(tx) => {
                            let pos = if sink.empty() {
                                Duration::ZERO
                            } else {
                                sink.get_pos()
                            };
                            tx.send(pos).unwrap();
                        }
                        PlayerControl::IsPaused(tx) => {
                            tx.send(sink.is_paused()).unwrap();
                        }
                        PlayerControl::IsEmpty(tx) => {
                            tx.send(sink.empty()).unwrap();
                        },
                        PlayerControl::Drain(tx) => {
                            sink.sleep_until_end();
                            tx.send(()).unwrap();
                        }
                    }
                }
            }
        });
        let player = Player {
            _player_thread: Arc::new(player_thread),
            ctrl_tx,
            current_item: Arc::default(),
            stream_play: Arc::default(),
            buffer_progress: Arc::default(),
            content_length: Arc::default(),
            event_listener: event_listener.clone(),
            stop_by_user,
            event_tx,
            rt: Arc::new(runtime),
        };

        // Start callback thread
        thread::spawn(
            move || {
                for x in event_rx {
                    if let Some(listener) = event_listener.read().unwrap().as_ref() {
                        listener.on_event(x);
                    }
                }
            }
        );
        player
    }

    // Should we call it `replace_media_item`?
    pub async fn append_media_item(&self, item: MediaItem) -> Result<(), PlayerError> {
        info!("New media item: {:?}", item.audio_url);
        let buffer_progress = self.buffer_progress.clone();

        let current_item = self.current_item.clone();
        let stop_by_user = self.stop_by_user.clone();
        let ctrl_tx = self.ctrl_tx.clone();
        self.rt
            .spawn(async move {
                current_item.write().unwrap().replace(item.clone());
                *stop_by_user.lock().unwrap() = false;
                let storage_provider = TempStorageProvider::new();

                let audio_url =
                    Url::parse(&item.audio_url).map_err(|x| URLError { msg: x.to_string() })?;

                let source: Box<dyn Source + Send + Sync> = match audio_url.scheme() {
                    "file" => {
                        info!("Local file");
                        *buffer_progress.write().unwrap() = 1f32;
                        let path = &item.audio_url.as_str().strip_prefix("file://").ok_or_else(|| {
                            IOError { msg: "Invalid file URL".to_string() }
                        })?;
                        println!("Path: {}", path);
                        let file = File::open(path)
                            .map_err(|x| IOError { msg: x.to_string() })?;
                        let length = file.metadata().map_err(|x| IOError { msg: x.to_string() })?.len();

                        let decoder = tokio::task::spawn_blocking({
                            let format = (&item.format).clone();
                            move || {
                                Decoder::builder()
                                    .with_seekable(true)
                                    .with_byte_len(length)
                                    .with_data(file)
                                    .with_hint(&format)
                                    .build()
                            }
                        }).await.unwrap().map_err(|x| RodioError { msg: x.to_string() })?;
                        info!("Format: hint: {}, sample_rate: {}, channels: {}, total duration: {:?}", item.format, decoder.sample_rate(), decoder.channels(), decoder.total_duration());
                        Box::new(decoder)
                    }
                    "https" => {
                        info!("Remote file");
                        *buffer_progress.write().unwrap() = 0f32;
                        let (tx, rx) = mpsc::channel();
                        let settings = Settings::default().on_progress({
                            let tx = tx;
                            move |stream, state, _| on_progress(&tx, stream, state)
                        });

                        thread::spawn(move || {
                            for x in rx {
                                *buffer_progress.write().unwrap() = x;
                            }
                        });

                        let stream = StreamDownload::new_http(audio_url.clone(), storage_provider, settings)
                            .await
                            .map_err(|x| IOError { msg: x.to_string() })?;
                        let length = stream.content_length().ok_or_else(|| IOError { msg: "Content length is not available!".to_string() })?;
                        info!("Creating decoder");
                        let decoder = tokio::task::spawn_blocking({
                            let format = item.format.clone();
                            move || {
                                Decoder::builder()
                                    .with_seekable(true)
                                    .with_byte_len(length)
                                    .with_data(stream)
                                    .with_hint(&format)
                                    .build()
                            }
                        }).await.unwrap().map_err(|x| RodioError { msg: x.to_string() })?;

                        info!("Format: hint: {}, sample_rate: {}, channels: {}, total duration: {:?}", item.format, decoder.sample_rate(), decoder.channels(), decoder.total_duration());
                        Box::new(decoder)
                    }
                    _ => Err(URLError {
                        msg: format!("Unsupported audio URL scheme: {}", audio_url),
                    })?,
                };

                if let Some(x) = current_item.write().unwrap().as_ref()
                    && x != &item {
                    // If this item is conflicted, do not append this item
                    return Ok(())
                } else {
                    ctrl_tx.send(PlayerControl::Append(source)).unwrap();
                }
                return Ok(())
            }).await.unwrap()
    }

    pub fn play(&self) {
        self.ctrl_tx.send(PlayerControl::Play).unwrap();
    }

    pub fn pause(&self) {
        self.ctrl_tx.send(PlayerControl::Pause).unwrap();
    }

    pub fn stop(&self) {
        let (tx, rx) = oneshot::channel();
        *self.stop_by_user.lock().unwrap() = true;
        self.ctrl_tx.send(PlayerControl::Stop(tx)).unwrap();
        rx.recv().unwrap();
    }

    pub fn seek(&self, pos: Duration) -> Result<(), PlayerError> {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::Seek(pos, tx)).unwrap();
        rx.recv().unwrap()?;
        Ok(())
    }

    pub fn set_volume(&self, volume: f32) {
        self.ctrl_tx.send(PlayerControl::SetVolume(volume)).unwrap();
    }

    pub fn volume(&self) -> f32 {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::GetVolume(tx)).unwrap();
        rx.recv().unwrap()
    }

    pub fn get_pos(&self) -> Duration {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::GetPos(tx)).unwrap();
        rx.recv().unwrap()
    }

    pub fn is_paused(&self) -> bool {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::IsPaused(tx)).unwrap();
        rx.recv().unwrap()
    }

    pub fn empty(&self) -> bool {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::IsEmpty(tx)).unwrap();
        rx.recv().unwrap()
    }

    pub fn drain(&self) {
        let (tx, rx) = oneshot::channel();
        self.ctrl_tx.send(PlayerControl::Drain(tx)).unwrap();
        rx.recv().unwrap()
    }

    pub fn buffer_progress(&self) -> f32 {
        *self.buffer_progress.read().unwrap()
    }

    pub fn set_event_listener(&self, listener: Arc<dyn PlayerEventListener>) {
        self.event_listener.write().unwrap().replace(listener);
    }

    fn send_event(&self, event: PlayerEvent) {
        self.event_tx.send(event).unwrap();
    }
}

fn on_progress(
    tx: &mpsc::Sender<f32>,
    stream: &HttpStream<stream_download::http::reqwest::Client>,
    state: StreamState,
) {
    match state.phase {
        StreamPhase::Prefetching {
            target, chunk_size, ..
        } => {
            let progress = (state.current_position as f32 / target as f32) * 100.0;
            debug!(
                "{:.2?} prefetch progress: {:.2}%, downloaded: {:?}, chunks: {}",
                state.elapsed, progress, state.current_chunk, chunk_size
            );
        }
        StreamPhase::Downloading { chunk_size, .. } => {
            let progress = if let Some(len) = stream.content_length() {
                state.current_position as f32 / len as f32
            } else {
                // Handle unknown length, e.g., based on position only or skip
                0.0
            };
            debug!(
                "{:.2?} download progress {:.2}%, downloaded: {:?}, chunks: {}",
                state.elapsed, progress, state.current_chunk, chunk_size
            );
            tx.send(progress).unwrap();
        }
        StreamPhase::Complete => {
            tx.send(1f32).unwrap();
            debug!("{:.2?} download complete", state.elapsed);
        }
        _ => {}
    }
}
