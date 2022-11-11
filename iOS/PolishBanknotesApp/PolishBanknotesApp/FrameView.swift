//
//  FrameView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 11/11/2022.
//

import SwiftUI

struct FrameView: View {
    var image: CGImage?
    private let label = Text("label")
    
    var body: some View {
        if let image = image {
            Image(image, scale: 1.0, orientation: .up, label: label)
        } else {
            Spacer()
        }
    }
}

struct FrameView_Previews: PreviewProvider {
    static var previews: some View {
        FrameView()
    }
}
