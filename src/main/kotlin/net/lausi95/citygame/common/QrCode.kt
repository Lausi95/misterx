package net.lausi95.citygame.common

import io.nayuki.qrcodegen.QrCode
import java.awt.image.BufferedImage


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
