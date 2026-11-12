package com.example.miniplayer

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class CropVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : VideoView(context, attrs, defStyle) {

    var cropEnabled = false
        set(value) {
            field = value
            requestLayout()
        }

    // 自己维护视频尺寸，来自底层 MediaPlayer
    private var mVideoWidth = 0
    private var mVideoHeight = 0

    init {
        // VideoView 内部就会设置 OnPreparedListener，这里不能覆盖
        // 改用 setOnInfoListener 之外的方式：在 onMeasure 前由外部同步
        // 更稳妥：重写 setVideoURI 后手动挂载 MediaPlayer 的 listener 不可靠
        // 所以这里改为提供一个公开方法，由 Activity 在 prepared 后调用
    }

    fun syncVideoSize(width: Int, height: Int) {
        if (width > 0 && height > 0 && (width != mVideoWidth || height != mVideoHeight)) {
            mVideoWidth = width
            mVideoHeight = height
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!cropEnabled || mVideoWidth <= 0 || mVideoHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (width <= 0 || height <= 0) {
            setMeasuredDimension(width, height)
            return
        }
        val videoRatio = mVideoWidth.toFloat() / mVideoHeight
        val viewRatio = width.toFloat() / height
        if (videoRatio > viewRatio) {
            setMeasuredDimension((height * videoRatio).toInt(), height)
        } else {
            setMeasuredDimension(width, (width / videoRatio).toInt())
        }
    }
}
