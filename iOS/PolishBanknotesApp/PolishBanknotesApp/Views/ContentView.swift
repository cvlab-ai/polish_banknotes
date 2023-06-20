//
//  ContentView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI
import CoreML

struct ContentView: View {
    @StateObject private var model = ContentViewModel()
    
    @State private var isImporting: Bool = false
    
    private func onCompletion(result: Result<[URL], Error>) -> Void {
        do {
            let selectedFile: URL = try result.get().first!
            model.setModel(path: selectedFile)
        } catch {
            print("Unable to read file contents")
            print(error.localizedDescription)
        }
    }
        
    var body: some View {
        ZStack {
            FrameView(image: model.frame)
                .edgesIgnoringSafeArea(.all)
                .accessibilityHidden(true)
            
            VStack {
                if (model.developerMode) {
                    DeveloperView(modelName: model.getModelName(),
                                  predictions: model.predictions.getTopNPredictions(n: 3),
                                  onCompletion: self.onCompletion)
                } else {
                    Text(model.predictions.classLabel)
                        .accessibilityHidden(false)
                        .foregroundColor(.yellow)
                        .font(.system(size: 190))
                        .shadow(color: .black, radius: 1)
                }
            }
            
            ErrorView(error: model.error)
                .accessibilityLabel("Error message.")
                .accessibilityValue(model.error?.localizedDescription ?? "")
                .accessibilityHint("Something went wrong.")
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
