//
//  Classifier.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 12/11/2022.
//

import AVFoundation
import Vision
import UIKit

extension ViewController {
    func setupClassifier() {
        guard let visionModel = try? VNCoreMLModel(for: Resnet50(configuration: MLModelConfiguration()).model) else { fatalError("App failed to create model") }
        let classifications = VNCoreMLRequest(model: visionModel, completionHandler: classificationDidComplete)
        self.requests = [classifications]
    }
    
    func setupLabel() {
        DispatchQueue.main.async { [unowned self] in
            self.view.addSubview(self.identifierLabel)
            identifierLabel.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -32).isActive = true
            identifierLabel.leftAnchor.constraint(equalTo: view.leftAnchor).isActive = true
            identifierLabel.rightAnchor.constraint(equalTo: view.rightAnchor).isActive = true
            identifierLabel.heightAnchor.constraint(equalToConstant: 50).isActive = true
        }
    }
    
    func classificationDidComplete(request: VNRequest, error: Error?) {
        
        guard let results = request.results as? [VNClassificationObservation] else { return }
        guard let firstObservation = results.first else { return }
        
        DispatchQueue.main.async {
            self.identifierLabel.text = "\(firstObservation.identifier) \(firstObservation.confidence * 100)%"
        }
    }
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        let imageRequestHandler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, orientation: .up, options: [:])
        
        do {
            try imageRequestHandler.perform(self.requests)
        } catch let error {
            print(error)
        }
    }
}
