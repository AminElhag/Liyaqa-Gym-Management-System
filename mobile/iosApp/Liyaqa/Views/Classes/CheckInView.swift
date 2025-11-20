import SwiftUI
import AVFoundation

struct CheckInView: View {
    @State private var isScanning = false
    @State private var scannedCode: String?
    @State private var showingManualEntry = false
    @State private var manualCode = ""
    @State private var checkInStatus: CheckInStatus?

    enum CheckInStatus {
        case success
        case error(String)
    }

    var body: some View {
        ZStack {
            if isScanning {
                QRCodeScannerView(scannedCode: $scannedCode, isScanning: $isScanning)
                    .edgesIgnoringSafeArea(.all)
            } else {
                checkInContent
            }
        }
        .navigationTitle("Check In")
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: scannedCode) { newValue in
            if let code = newValue {
                processCheckIn(code: code)
            }
        }
        .sheet(isPresented: $showingManualEntry) {
            ManualCheckInView(code: $manualCode) {
                processCheckIn(code: manualCode)
                showingManualEntry = false
            }
        }
    }

    // MARK: - Check-In Content
    private var checkInContent: some View {
        VStack(spacing: 30) {
            Spacer()

            // Icon
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 100))
                .foregroundColor(.liyaqaBrand)

            // Title
            VStack(spacing: 8) {
                Text("Ready to Check In?")
                    .font(.headlineMedium)
                    .foregroundColor(.textPrimary)

                Text("Scan the QR code at the gym entrance or enter your member ID manually")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            Spacer()

            // Action Buttons
            VStack(spacing: 16) {
                Button {
                    requestCameraPermission()
                } label: {
                    HStack {
                        Image(systemName: "qrcode.viewfinder")
                        Text("Scan QR Code")
                    }
                    .primaryButtonStyle()
                }

                Button {
                    showingManualEntry = true
                } label: {
                    HStack {
                        Image(systemName: "keyboard")
                        Text("Enter Member ID")
                    }
                    .secondaryButtonStyle()
                }
            }
            .padding(.horizontal)

            // Status Message
            if let status = checkInStatus {
                statusView(status)
            }

            Spacer()
        }
        .padding()
    }

    // MARK: - Status View
    private func statusView(_ status: CheckInStatus) -> some View {
        Group {
            switch status {
            case .success:
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.success)
                    Text("Check-in successful!")
                        .font(.bodyMedium)
                        .foregroundColor(.success)
                }
                .padding()
                .background(Color.success.opacity(0.1))
                .cornerRadius(12)

            case .error(let message):
                HStack {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.error)
                    Text(message)
                        .font(.bodyMedium)
                        .foregroundColor(.error)
                }
                .padding()
                .background(Color.error.opacity(0.1))
                .cornerRadius(12)
            }
        }
    }

    // MARK: - Helper Methods
    private func requestCameraPermission() {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            DispatchQueue.main.async {
                if granted {
                    isScanning = true
                } else {
                    checkInStatus = .error("Camera access denied. Please enable it in Settings.")
                }
            }
        }
    }

    private func processCheckIn(code: String) {
        // TODO: Implement actual check-in logic with API
        // For now, simulate success
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            checkInStatus = .success
            scannedCode = nil

            // Reset after showing success
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                checkInStatus = nil
            }
        }
    }
}

// MARK: - QR Code Scanner View
struct QRCodeScannerView: UIViewControllerRepresentable {
    @Binding var scannedCode: String?
    @Binding var isScanning: Bool

    func makeUIViewController(context: Context) -> QRScannerViewController {
        let controller = QRScannerViewController()
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: QRScannerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, QRScannerDelegate {
        let parent: QRCodeScannerView

        init(_ parent: QRCodeScannerView) {
            self.parent = parent
        }

        func didScanCode(_ code: String) {
            parent.scannedCode = code
            parent.isScanning = false
        }

        func didFailWithError(_ error: Error) {
            parent.isScanning = false
        }
    }
}

// MARK: - QR Scanner Delegate
protocol QRScannerDelegate: AnyObject {
    func didScanCode(_ code: String)
    func didFailWithError(_ error: Error)
}

// MARK: - QR Scanner View Controller
class QRScannerViewController: UIViewController {
    weak var delegate: QRScannerDelegate?
    private var captureSession: AVCaptureSession?

    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
    }

    private func setupCamera() {
        captureSession = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            delegate?.didFailWithError(NSError(domain: "Camera not available", code: 0))
            return
        }

        let videoInput: AVCaptureDeviceInput

        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            delegate?.didFailWithError(error)
            return
        }

        if captureSession?.canAddInput(videoInput) == true {
            captureSession?.addInput(videoInput)
        } else {
            delegate?.didFailWithError(NSError(domain: "Cannot add video input", code: 0))
            return
        }

        let metadataOutput = AVCaptureMetadataOutput()

        if captureSession?.canAddOutput(metadataOutput) == true {
            captureSession?.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        } else {
            delegate?.didFailWithError(NSError(domain: "Cannot add metadata output", code: 0))
            return
        }

        let previewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.captureSession?.startRunning()
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        captureSession?.stopRunning()
    }
}

extension QRScannerViewController: AVCaptureMetadataOutputObjectsDelegate {
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        captureSession?.stopRunning()

        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let stringValue = readableObject.stringValue else { return }

            AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            delegate?.didScanCode(stringValue)
        }
    }
}

// MARK: - Manual Check-In View
struct ManualCheckInView: View {
    @Binding var code: String
    let onSubmit: () -> Void
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Text("Enter your member ID to check in")
                    .font(.bodyMedium)
                    .foregroundColor(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding()

                TextField("Member ID", text: $code)
                    .textFieldStyle(icon: "number")
                    .keyboardType(.numberPad)
                    .padding(.horizontal)

                Button {
                    onSubmit()
                } label: {
                    Text("Check In")
                        .primaryButtonStyle(isEnabled: !code.isEmpty)
                }
                .disabled(code.isEmpty)
                .padding(.horizontal)

                Spacer()
            }
            .padding(.top)
            .navigationTitle("Manual Check-In")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .foregroundColor(.textPrimary)
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        CheckInView()
    }
}
