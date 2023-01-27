//
//  PredictionsView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct PredictionsView: View {
    var predictions: [(String, Double)]
    var developerMode: Bool
    
    var body: some View {
        VStack {
            ForEach(predictions, id: \.0) { prediction in
                Text("\(prediction.0) \(String(format: "%.0f", (prediction.1 * 100)))%")
                    .foregroundColor(.white)
                    .shadow(color: .black, radius: 1)
            }
        }
    }
}

struct PredictionsView_Previews: PreviewProvider {
    static let predictions = [("class1", 0.6), ("class2", 0.3), ("class3", 0.1)]
    
    static var previews: some View {
        PredictionsView(predictions: predictions, developerMode: true)
    }
}
