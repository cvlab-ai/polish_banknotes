//
//  ContentView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 09/11/2022.
//

import SwiftUI

struct ContentView: View {
    @StateObject private var model = FrameHandler()
    
    var body: some View {
        FrameView(image: model.frame)
            .ignoresSafeArea()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
