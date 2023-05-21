//
//  Classifier.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import CoreML

class Classifier {
    @Published var model: MLModel?
    @Published var error: Error?
    
    static let shared = Classifier()
    private let defaultModelURL = Bundle.main.url(forResource: "model_ml1", withExtension: "mlmodelc")!
    private var currentModelURL: URL;
    private let dispatchQueue = DispatchQueue(label: "classifier.queue", qos: .userInitiated)
    
    private init() {
        currentModelURL = defaultModelURL
        setDefaultModel()
    }
    
    private func setSubscription() {
        self.$model
            .assign(to: &$model)
    }
    
    private func set(error: Error) {
        DispatchQueue.main.async {
            self.error = error
        }
    }
    
    private func set(model: MLModel) {
        DispatchQueue.main.async {
            self.model = model
        }
    }
    
    private func setDefaultModel() {
        do {
            model = try MLModel.init(contentsOf: defaultModelURL)
        } catch {
            set(error: error)
            return
        }
    }
    
    func setModel(path: URL) {
        dispatchQueue.async {
            defer { path.stopAccessingSecurityScopedResource() }
            
            do {
                if !path.startAccessingSecurityScopedResource() {
                    throw ClassifierError.accessDenied // cannot access model error
                }
                let modelURL = try MLModel.compileModel(at: path)
                let model = try MLModel.init(contentsOf: modelURL)
                
                self.set(model: model)
                self.currentModelURL = modelURL
            } catch {
                self.set(error: error)
                return
            }
        }
    }
    
    func getModelName() -> String {
        return currentModelURL.lastPathComponent
    }
    
}

class ClassifierInput: MLFeatureProvider {
    var image: CVPixelBuffer
    
    var featureNames: Set<String> {
        get {
            return ["image"]
        }
    }
    
    func featureValue(for featureName: String) -> MLFeatureValue? {
        if (featureName == "image") {
            return MLFeatureValue(pixelBuffer: image)
        }
        return nil
    }
    
    convenience init(imageWith image: CGImage) throws {
        self.init(image: try MLFeatureValue(cgImage: image, pixelsWide: 224, pixelsHigh: 224, pixelFormatType: kCVPixelFormatType_32BGRA, options: nil).imageBufferValue!)
    }
    
    init(image: CVPixelBuffer) {
        self.image = image
    }
}

class ClassifierOutput: MLFeatureProvider {
    private let provider : MLFeatureProvider

    /// Probability of each category as dictionary of strings to doubles
    var classLabelProbs: [String : Double] {
        return self.provider.featureValue(for: "Identity")!.dictionaryValue as! [String : Double]
    }

    /// Most likely image category as string value
    var classLabel: String {
        return self.provider.featureValue(for: "classLabel")!.stringValue
    }

    var featureNames: Set<String> {
        return self.provider.featureNames
    }
    
    func featureValue(for featureName: String) -> MLFeatureValue? {
        return self.provider.featureValue(for: featureName)
    }

    init(classLabelProbs: [String : Double], classLabel: String) {
        self.provider = try! MLDictionaryFeatureProvider(dictionary: ["Identity" : MLFeatureValue(dictionary: classLabelProbs as [AnyHashable : NSNumber]), "classLabel" : MLFeatureValue(string: classLabel)])
    }

    init(features: MLFeatureProvider) {
        self.provider = features
    }
}
