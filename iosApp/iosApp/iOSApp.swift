import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        HelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}