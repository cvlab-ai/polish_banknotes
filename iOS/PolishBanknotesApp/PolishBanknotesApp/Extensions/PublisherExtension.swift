//
//  PublisherExtension.swift
//  CameraTest
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import Combine
import CoreML

extension Publisher where Self.Output: MLFeatureProvider {
  public func prediction(model: MLModel)
              -> Publishers.Map<Self, Result<MLFeatureProvider, Error>> {
    map { input in
      do {
        return .success(try model.prediction(from: input))
      } catch {
        return .failure(error)
      }
    }
  }
}
