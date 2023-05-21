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
    private var frameCount = 0
    private let classifier = Classifier.shared
    
    private var framePredictionSubscription: AnyCancellable?;
    private let predictionRate = 5
    private let predictionSize = 5
    
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
        
        // fps ~= 30, predictionRate = 2 => ~ 15fps (every 2nd frame)
        framePredictionSubscription = predictionSubscription()
    }
    
    func predictionSubscription() -> AnyCancellable {
        return frameManager.$current
            .compactMap { $0 } // check for nil buffers
            .map { buffer in
                self.frameCount += 1
                return buffer
            }
            .filter { _ in self.frameCount % self.predictionRate == 0 }
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
            .collect(predictionSize)
            .map { predictions in // here will be functionality of merging multiple predictions into one output
                return predictions[0]
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
        
        self.framePredictionSubscription = self.predictionSubscription()
    }
}
