//
//  ContentView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 09/11/2022.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        HostedViewController()
            .ignoresSafeArea()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
