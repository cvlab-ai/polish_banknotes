//
//  ClassifierError.swift
//  PolishBanknotesApp
//
//  Created by Miłosz Chojnacki on 07/01/2023.
//

import Foundation

enum ClassifierError: Error {
    case accessDenied
}

extension ClassifierError: LocalizedError {
    var errorDescription: String? {
          switch self {
          case .accessDenied:
              return "Access denied to .mlmodel file"
          }
    }
}
