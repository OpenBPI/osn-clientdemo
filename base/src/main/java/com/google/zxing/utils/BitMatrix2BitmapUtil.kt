package com.google.zxing.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * 将条码数据转换成Bitmap
 */
class BitMatrix2BitmapUtil {

    /**
     * 创建二维码
     */
    fun createQRCode(context: String,
                     point: Point= Point(300, 300),
                     hints: MutableMap<EncodeHintType, *>? = null,
                     colors: IntArray = intArrayOf(Color.BLACK, Color.WHITE)): Bitmap{
        //生成二维码数据
        val matrix = QRCodeWriter().encode(context, BarcodeFormat.QR_CODE, point.x, point.y, hints)
        //将二维码数据转换成像素
        val pixels = getPixel(matrix, point, colors)
        //将像素还原成bitmap
        val bit = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888)
        bit.setPixels(pixels, 0, point.x, 0, 0, point.x, point.y)
        return bit
    }

    private fun getPixel(matrix: BitMatrix, point: Point, colors: IntArray): IntArray {
        val pixels = IntArray(point.x * point.y)
        for (y in 0 until point.y) {
            for (x in 0 until point.x) {
                if (matrix.get(x, y)) {
                    // 黑色色块像素设置
                    pixels[y * point.x + x] = colors[0]
                } else {
                    // 白色色块像素设置
                    pixels[y * point.x + x] = colors[1]
                }
            }
        }
        return pixels
    }
}