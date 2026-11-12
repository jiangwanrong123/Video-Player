package com.example.miniplayer

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class CropVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : VideoView(context, attrs, defStyle) {

    // true 时按 center-crop 测量（铺满、裁切边缘）；false 时保持原始比例
    var cropEnabled = false
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!cropEnabled) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val vw = videoWidth
        val vh = videoHeight
        if (vw > 0 && vh > 0 && width > 0 && height > 0) {
            val videoRatio = vw.toFloat() / vh
            val viewRatio = width.toFloat() / height
            if (videoRatio > viewRatio) {
                // 视频比屏幕更宽：高度铺满，宽度溢出被裁
                setMeasuredDimension((height * videoRatio).toInt(), height)
            } else {
                // 视频比屏幕更高：宽度铺满，高度溢出被裁
                setMeasuredDimension(width, (width / videoRatio).toInt())
            }
        } else {
            setMeasuredDimension(width, height)
        }
    }
}
