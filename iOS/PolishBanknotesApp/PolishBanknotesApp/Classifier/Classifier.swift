//
//  Classifier.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import CoreML

class Classifier {
    @Published var model: MLModel?
    @Published var error: ClassifierError?
    
    static let shared = Classifier()
    private let defaultModelURL = Bundle.main.url(forResource: "Resnet50", withExtension: "mlmodelc")!
    
    private init() {
        configure()
    }
    
    private func set(error: ClassifierError) {
        DispatchQueue.main.async {
            self.error = error
        }
    }
    
    private func configure() {
        model = try? MLModel.init(contentsOf: defaultModelURL)
        if model == nil {
            set(error: .classifierLoadFail)
        }
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
        return self.provider.featureValue(for: "classLabelProbs")!.dictionaryValue as! [String : Double]
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
        self.provider = try! MLDictionaryFeatureProvider(dictionary: ["classLabelProbs" : MLFeatureValue(dictionary: classLabelProbs as [AnyHashable : NSNumber]), "classLabel" : MLFeatureValue(string: classLabel)])
    }

    init(features: MLFeatureProvider) {
        self.provider = features
    }
}
