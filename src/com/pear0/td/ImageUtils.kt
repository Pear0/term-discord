package com.pear0.td

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Created by William on 3/26/2017.
 */
object ImageUtils {

    private val USE_FULL_RGB = true

    private class ColorBucket {

        private var red: Float = 0f
        private var green: Float = 0f
        private var blue: Float = 0f

        private var max: Float = 0f

        fun addColor(color: Int) {
            Color(color).let {
                red += it.red * it.alpha / (255f * 255f)
                green += it.green * it.alpha / (255f * 255f)
                blue += it.blue * it.alpha / (255f * 255f)

                max += it.alpha / 255f
            }
        }

        fun getFinalColor() = Color(red / max, green / max, blue / max)

    }

    private infix fun Int.divUp(that: Int) = Math.ceil(this / that.toDouble()).toInt()

    private fun returnStrPos(g: Double)//takes the grayscale value as parameter
            : Char {
        val str: Char

        if (g >= 230.0) {
            str = ' '
        } else if (g >= 200.0) {
            str = '.'
        } else if (g >= 180.0) {
            str = '*'
        } else if (g >= 160.0) {
            str = ':'
        } else if (g >= 130.0) {
            str = 'o'
        } else if (g >= 100.0) {
            str = '&'
        } else if (g >= 70.0) {
            str = '8'
        } else if (g >= 50.0) {
            str = '#'
        } else {
            str = '@'
        }
        return str // return the character

    }

    fun renderASCIIArt(image: BufferedImage, pixelsPerChar: Int): TextImage {
        val textImage = BasicTextImage(image.width divUp pixelsPerChar, image.height divUp pixelsPerChar)

        for (textY in 0 until textImage.size.rows) {
            val yRange = (image.height * textY / textImage.size.rows) until (image.height * (textY + 1) / textImage.size.rows)

            for (textX in 0 until textImage.size.columns) {
                val xRange = (image.width * textX / textImage.size.columns) until (image.width * (textX + 1) / textImage.size.columns)

                val bucket = ColorBucket()

                for (y in yRange) {
                    for (x in xRange) {
                        bucket.addColor(image.getRGB(x, y))
                    }
                }

                val color = bucket.getFinalColor()

                val foreground = color.let {
                    if (USE_FULL_RGB) TextColor.RGB(it.red, it.green, it.blue)
                    else TextColor.Indexed.fromRGB(it.red, it.green, it.blue)
                }

                val gValue = color.red * 0.2989 + color.blue * 0.5870 + color.green * 0.1140

                textImage.setCharacterAt(textX, textY, TextCharacter(returnStrPos(gValue), foreground, TextColor.ANSI.BLACK))

            }
        }

        return textImage
    }

    fun renderASCIIArt(image: BufferedImage, size: TerminalSize): TextImage {
        val pixelsPerChar = Math.max(image.width divUp size.columns, image.height divUp size.rows)

        val partial = renderASCIIArt(image, pixelsPerChar)

        val textImage = BasicTextImage(size)

        partial.copyTo(textImage, 0, partial.size.rows, 0, partial.size.columns,
                (size.rows - partial.size.rows) / 2, (size.columns - partial.size.columns) / 2)

        return textImage
    }

}