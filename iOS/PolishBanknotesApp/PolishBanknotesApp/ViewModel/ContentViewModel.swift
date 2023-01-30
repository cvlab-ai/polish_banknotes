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
    
    var developerMode = true
    
    private let cameraManager = CameraManager.shared
    private let frameManager = FrameManager.shared
    private let classifier = Classifier.shared
    
    private var framePredictionSubscription: AnyCancellable?;
    
    @Published private var model: MLModel?
    
    init() {
        setupSubscriptions()
    }
    
    func getModelName() -> String {
        return classifier.getModelName()
    }
    
    func setModel(path: URL) {
        classifier.setModel(path: path)
        
        framePredictionSubscription?.cancel()
        
        framePredictionSubscription = frameManager.$current
            .compactMap { $0 } // check for nil buffers
            .compactMap { buffer in
                let image = CGImage.create(from: buffer)
                return try? ClassifierInput(imageWith: image!)
            }
            .prediction(model: self.model!)
            .compactMap { try? $0.get() }
            .map {
                let output = ClassifierOutput(features: $0)
                return output
            }
            .sink { [weak self] predictions in
                self?.predictions = predictions
            }
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
        
        classifier.$model
            .compactMap { model in
                return model
            }
            .assign(to: &$model)
        
        framePredictionSubscription = frameManager.$current
            .compactMap { $0 } // check for nil buffers
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
            .sink { [weak self] predictions in
                self?.predictions = predictions
            }
    }
}
