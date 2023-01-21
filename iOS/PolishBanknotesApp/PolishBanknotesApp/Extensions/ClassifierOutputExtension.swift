//
//  predictionsExtension.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 19/01/2023.
//

import Foundation

extension ClassifierOutput {
    func getTopNPredictions(n: Int) -> Array<(String, Double)> {
        if abs(n) > self.classLabelProbs.count { return [] }
        return Array(self.classLabelProbs.sorted { $0.value > $1.value }.map { ($0.key, $0.value) }[..<n])
    }
}
