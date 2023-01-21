//
//  ContentViewModel.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import Foundation
import CoreImage
import Vision
import Combine

class ContentViewModel: ObservableObject {
    @Published var error: Error?
    @Published var frame: CGImage?
    @Published var predictions: ClassifierOutput = ClassifierOutput(classLabelProbs: ["": 0], classLabel: "")
    
    var developerMode = false
    
    private let cameraManager = CameraManager.shared
    private let frameManager = FrameManager.shared
    private let classifier = Classifier.shared
    
    init() {
        setupSubscriptions()
    }
    
    func setupSubscriptions() {
        
        cameraManager.$error
            .receive(on: RunLoop.main)
            .map { $0 }
            .assign(to: &$error)
        
        frameManager.$current
            .receive(on: RunLoop.main)
            .compactMap { buffer in
                return CGImage.create(from: buffer)
            }
            .assign(to: &$frame)
        
        frameManager.$current
            .compactMap { $0 } // TODO: check for nil buffers
            .compactMap { buffer in
                let image = CGImage.create(from: buffer)
                return try? ClassifierInput(imageWith: image!)
            }
            .prediction(model: classifier.model!)
            .compactMap { try? $0.get() }
            .map {
                let output = ClassifierOutput(features: $0)
                return output
            }
            .assign(to: &$predictions)
    }
}
