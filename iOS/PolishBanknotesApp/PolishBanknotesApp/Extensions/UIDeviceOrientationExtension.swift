//
//  UIDeviceOrientationExtension.swift
//  CameraTest
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import SwiftUI

extension UIDeviceOrientation {
    var imageOrientation: Image.Orientation {
        switch UIDevice.current.orientation {
        case .landscapeLeft:
          return .left
        case .landscapeRight:
            return .right
        case .portrait:
            return .up
        case .portraitUpsideDown:
            return .down
        default:
            return .up
        }
    }
}
