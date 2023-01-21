//
//  ContentView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct ContentView: View {
  @StateObject private var model = ContentViewModel()

  var body: some View {
      ZStack {
          FrameView(image: model.frame)
              .edgesIgnoringSafeArea(.all)
              .accessibilityHidden(true)
          
          VStack {
              if (model.developerMode) {
                  DeveloperView()
                  Spacer()
                  PredictionsView(predictions: model.predictions.getTopNPredictions(n: 3), developerMode: model.developerMode)
              } else {
                  Spacer()
                  Text(model.predictions.classLabel)
                      .accessibilityHidden(false)
                      .foregroundColor(.white)
                      .shadow(color: .black, radius: 1)
              }
          }
        
          ErrorView(error: model.error)
              .accessibilityLabel("")
              .accessibilityHint("")
    }
      .accessibilityLabel("Predicted value.")
      .accessibilityValue("\(model.predictions.classLabel).")
      .accessibilityHint("Most likely banknote nominal.")
      .onLongPressGesture(minimumDuration: 10) {
        model.developerMode = !model.developerMode
      }
  }
}

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
