//
//  FrameView.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

struct FrameView: View {
  var image: CGImage?
  @State private var orientation = UIDeviceOrientation.unknown

  private let label = Text("Video feed")

  var body: some View {
    if let image = image {
      GeometryReader { geometry in
        Image(image, scale: 1.0, orientation: orientation.imageOrientation, label: label)
          .resizable()
          .scaledToFill()
          .frame(
            width: geometry.size.width,
            height: geometry.size.height,
            alignment: .center)
          .clipped()
      }
      .onRotate { newOrientation in
        orientation = newOrientation
      }
    } else {
      EmptyView()
    }
  }
}

struct CameraView_Previews: PreviewProvider {
  static var previews: some View {
    FrameView(image: nil)
  }
}

