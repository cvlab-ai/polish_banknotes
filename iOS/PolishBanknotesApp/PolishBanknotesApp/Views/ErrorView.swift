//
//  ErrorView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct ErrorView: View {
  var error: Error?

  var body: some View {
    VStack {
      Text(error?.localizedDescription ?? "")
        .bold()
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(8)
        .foregroundColor(.white)
        .background(Color.red.edgesIgnoringSafeArea(.top))
        .opacity(error == nil ? 0.0 : 1.0)
        .animation(.easeInOut, value: 0.25)

      Spacer()
    }
  }
}

struct ErrorView_Previews: PreviewProvider {
  static var previews: some View {
    ErrorView(error: CameraError.cannotAddInput)
  }
}

