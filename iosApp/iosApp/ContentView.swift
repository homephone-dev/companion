import SwiftUI
import ComposeApp

/// Standard CMP iOS integration pattern: a thin SwiftUI wrapper hosting
/// the UIViewController produced by MainViewController.kt, which in turn
/// hosts the shared Compose UI (HomephoneAdminApp).
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
