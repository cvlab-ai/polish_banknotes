//
//  File.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 05/06/2023.
//

import Foundation
import Accessibility

class AccessibilityAnnouncementQueue {
    static let shard = AccessibilityAnnouncementQueue()
    private var queue: [String] = []

    private init() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(announcementFinished(_:)),
                                               name: UIAccessibility.announcementDidFinishNotification,
                                               object: nil)
    }
    
    func post(announcement: String) {
        guard UIAccessibility.isVoiceOverRunning else { return }
        
        queue.append(announcement)
        postNotification(announcement)
    }
    
    
    private func postNotification(_ message: String) {
        let attrMessage: NSAttributedString = NSAttributedString(string: message, attributes: [.accessibilitySpeechQueueAnnouncement: true])
        UIAccessibility.post(notification: .announcement, argument: attrMessage)
    }
    
    @objc private func announcementFinished(_ sender: Notification) {
        guard
            let userInfo = sender.userInfo,
            let firstQueueItem = queue.first,
            let announcement = userInfo[UIAccessibility.announcementStringValueUserInfoKey] as? String,
            let success = userInfo[UIAccessibility.announcementWasSuccessfulUserInfoKey] as? Bool,
            firstQueueItem == announcement
        else { return }
        
        if success {
            queue.removeFirst()
        } else {
            postNotification(firstQueueItem)
        }
    }
    
}
