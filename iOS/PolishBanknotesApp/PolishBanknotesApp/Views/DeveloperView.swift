//
//  DeveloperView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct DeveloperView: View {
    @State private var isImporting: Bool = false
    let modelName: String?
    var predictions: [(String, Double)]
    let onCompletion: (Result<[URL], Error>) -> Void
    
    var body: some View {
        VStack {
            Text("Developer mode")
                .bold()
                .foregroundColor(.white)
                .shadow(color: .black, radius: 1)
            
            Spacer()
            
            VStack {
                HStack {
                    Text("Currnet model: \(modelName ?? "")")
                        .foregroundColor(.white)
                    
                    Button("Load model") { isImporting.toggle() }
                        .fileImporter(isPresented: $isImporting,
                                      allowedContentTypes: [.init(filenameExtension: "mlmodel")!],
                                      allowsMultipleSelection: false,
                                      onCompletion: onCompletion)
                        .buttonStyle(.borderedProminent)
                }
                
                PredictionsView(predictions: predictions)
                    
            }
            .padding([.top], 10)
            .frame(maxWidth: .infinity)
            .background(.black)
            .ignoresSafeArea()
        }
    }
    
    struct DeveloperView_Previews: PreviewProvider {
        static let predictions = [("class1", 0.6), ("class2", 0.3), ("class3", 0.1)]
        
        static var previews: some View {
            DeveloperView(modelName: "ModelName", predictions: predictions) { _ in
                print("Imported model")
            }
        }
    }
}
