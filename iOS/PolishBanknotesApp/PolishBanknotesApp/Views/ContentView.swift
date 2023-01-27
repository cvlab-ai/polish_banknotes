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
    
    var body: some View {
        ZStack {
            FrameView(image: model.frame)
                .edgesIgnoringSafeArea(.all)
                .accessibilityHidden(true)
            
            VStack {
                if (model.developerMode) {
                    DeveloperView()
                    Spacer()
                    
                    Group {
                        HStack {
                            Text("Currnet model: \(model.getModelName())")
                            
                            Button("Load model") { isImporting.toggle() }
                                .fileImporter(isPresented: $isImporting,
                                              allowedContentTypes: [.init(filenameExtension: "mlmodel")!],
                                              allowsMultipleSelection: false) { result in
                                    do {
                                        let selectedFile: URL = try result.get().first!
                                        model.setModel(path: selectedFile)
                                    } catch {
                                        print("Unable to read file contents")
                                        print(error.localizedDescription)
                                    }
                                }
                                              .padding(10)
                                              .clipShape(Capsule(style: .circular))
                                              .background(Color(red: 0.3, green: 0.3, blue: 1))
                                              .foregroundColor(.white)
                            
                        }
                        
                        PredictionsView(predictions: model.predictions.getTopNPredictions(n: 3), developerMode: model.developerMode)
                            .padding(.top, 10.0)
                    }
                    .ignoresSafeArea()
                    .background(Color(red: 0, green: 0, blue: 0))
                } else {
                    Spacer()
                    Text(model.predictions.classLabel)
                        .accessibilityHidden(false)
                        .foregroundColor(.white)
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
