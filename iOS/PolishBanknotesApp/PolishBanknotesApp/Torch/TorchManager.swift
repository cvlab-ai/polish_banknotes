//
//  FlashlightManager.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 20/06/2023.
//

import Foundation
import AVFoundation
import UIKit

class TorchManager {
    var isOn = false
    
    func  activateTorch(interval: TimeInterval) {
        isOn = true
        DispatchQueue.global().async {
            self.toggleTorch(on: true)
            Thread.sleep(forTimeInterval: interval)
            self.toggleTorch(on: false)
        }
        isOn = false
    }
    
    func toggleTorch(on: Bool) {
        guard let device = AVCaptureDevice.default(for: AVMediaType.video) else { return }
        guard device.hasTorch else { print("Torch isn't available"); return }

        do {
            try device.lockForConfiguration()
            device.torchMode = on ? .on : .off
            if on { try device.setTorchModeOn(level: AVCaptureDevice.maxAvailableTorchLevel) }
            device.unlockForConfiguration()
            
            if on {
                UIAccessibility.post(notification: .announcement, argument: "Flash turned on!")
            } else {
                UIAccessibility.post(notification: .announcement, argument: "Flash turned off!")
            }
        } catch {
            print("Torch can't be used")
        }
    }
}
