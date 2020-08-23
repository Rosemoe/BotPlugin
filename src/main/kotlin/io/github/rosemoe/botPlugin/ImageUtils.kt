package io.github.rosemoe.botPlugin

import java.awt.Image
import java.awt.image.BufferedImage

fun rotateImage(src: Image, type: Boolean) : BufferedImage {
    return if (type) rotateImage90(src) else rotateImage180(src)
}

private fun rotateImage90(src: Image) : BufferedImage {
    val res = BufferedImage(src.getHeight(null), src.getWidth(null), BufferedImage.TYPE_INT_ARGB)
    res.accelerationPriority = 1f
    src.accelerationPriority = 1f
    res.createGraphics().apply {
        translate(src.getHeight(null) / 2.0, src.getWidth(null) / 2.0)
        rotate(Math.toRadians(90.0))
        translate(-src.getWidth(null) / 2.0, -src.getHeight(null) / 2.0)
        drawImage(src, null, null)
    }
    return res
}

private fun rotateImage180(src: Image) : BufferedImage {
    val res = BufferedImage(src.getWidth(null), src.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    res.accelerationPriority = 1f
    src.accelerationPriority = 1f
    res.createGraphics().apply {
        translate(src.getWidth(null) / 2.0, src.getHeight(null) / 2.0)
        rotate(Math.toRadians(180.0))
        translate(-src.getWidth(null) / 2.0, -src.getHeight(null) / 2.0)
        drawImage(src, null, null)
    }
    return res
}