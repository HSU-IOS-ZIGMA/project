import SwiftUI

extension Color {
    static let zigmaBlue = Color(red: 0.0, green: 0.25, blue: 0.56)
}

struct ZigmaButtonStyle: ButtonStyle {
    var background: Color = .zigmaBlue
    var foreground: Color = .white

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .semibold))
            .foregroundStyle(foreground)
            .padding(.vertical, 15)
            .padding(.horizontal, 16)
            .background(background.opacity(configuration.isPressed ? 0.82 : 1))
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

