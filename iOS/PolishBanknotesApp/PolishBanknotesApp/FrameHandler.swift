//
//  FrameHandler.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 11/11/2022.
//

import AVFoundation
import CoreImage

class FrameHandler: NSObject, ObservableObject {
    
    private var permissionGranted = false
    @Published var frame: CGImage?
    private let session = AVCaptureSession()
    private let videoDataOutput = AVCaptureVideoDataOutput()
    private let videoDataOutputQueue = DispatchQueue(label: "videoDataOutput", qos: .userInitiated, attributes: [], autoreleaseFrequency: .workItem)
    private let context = CIContext()
    
    
    override init() {
        super.init()
        checkPermission()
        videoDataOutputQueue.async { [unowned self] in
            self.setupCaptureSession()
            self.session.startRunning()
        }
    }
    
    private func checkPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            permissionGranted = true
            
        case .notDetermined:
            requestPermission()
            
        default:
            permissionGranted = false
        }
    }
    
    private func requestPermission() {
        AVCaptureDevice.requestAccess(for: .video) { [unowned self] granted in
            self.permissionGranted = granted
        }
    }
    
    private func setupCaptureSession() {
        guard permissionGranted else { return }
        
        let videoOutput = AVCaptureVideoDataOutput()
        
        guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else { return }
        guard let videoDeviceInput = try? AVCaptureDeviceInput(device: videoDevice) else { return }
        guard session.canAddInput(videoDeviceInput) else { return }
        session.addInput(videoDeviceInput)
        
        videoOutput.setSampleBufferDelegate(self, queue: DispatchQueue(label: "sampleBufferQueue"))
        session.addOutput(videoOutput)
        session.sessionPreset = .vga640x480
        videoOutput.connection(with: .video)?.videoOrientation = .portrait
    }
}

extension FrameHandler: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let cgImage = imageFromSampleBuffer(sampleBuffer: sampleBuffer) else { return }
        
        DispatchQueue.main.async { [unowned self] in
            self.frame = cgImage
        }
    }
    
    private func imageFromSampleBuffer(sampleBuffer: CMSampleBuffer) -> CGImage? {
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return nil }
        let ciImage = CIImage(cvPixelBuffer: imageBuffer)
        guard let cgImage = context.createCGImage(ciImage, from: ciImage.extent) else { return nil }
        
        return cgImage
    }
}
