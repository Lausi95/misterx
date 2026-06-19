package net.lausi95.citygame.common

import io.nayuki.qrcodegen.QrCode
import java.awt.image.BufferedImage

private const val QR_SCALE = 10
private const val QR_BORDER = 4
private const val QR_LIGHT_COLOR = 0xFFFFFF
private const val QR_DARK_COLOR = 0x000000

/**
 * Encodes [text] into a QR code rendered as a PNG-ready [BufferedImage] using the
 * application's standard rendering parameters (medium error correction, black on white).
 */
fun qrCodeImage(text: String): BufferedImage {
    val qrCode = QrCode.encodeText(text, QrCode.Ecc.MEDIUM)
    return qrCode.toImage(
        scale = QR_SCALE,
        border = QR_BORDER,
        lightColor = QR_LIGHT_COLOR,
        darkColor = QR_DARK_COLOR,
    )
}

fun QrCode.toImage(scale: Int, border: Int, lightColor: Int, darkColor: Int): BufferedImage {
    require(!(scale <= 0 || border < 0)) { "Value out of range" }
    require(!(border > Int.MAX_VALUE / 2 || size + border * 2L > Int.MAX_VALUE / scale)) { "Scale or border too large" }

    val result = BufferedImage(
        (size + border * 2) * scale,
        (size + border * 2) * scale,
        BufferedImage.TYPE_INT_RGB
    )
    for (y in 0..<result.height) {
        for (x in 0..<result.width) {
            val color: Boolean = getModule(x / scale - border, y / scale - border)
            result.setRGB(x, y, if (color) darkColor else lightColor)
        }
    }

    return result
}
