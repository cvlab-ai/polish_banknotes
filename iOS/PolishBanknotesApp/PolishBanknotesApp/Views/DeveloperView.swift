//
//  DeveloperView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct DeveloperView: View {
    var body: some View {
        VStack {
          Text("Developer mode")
            .bold()
            .foregroundColor(.white)
            .shadow(color: .black, radius: 1)
          
          Spacer()
        }
    }
}

struct DeveloperView_Previews: PreviewProvider {
    static var previews: some View {
        DeveloperView()
    }
}
