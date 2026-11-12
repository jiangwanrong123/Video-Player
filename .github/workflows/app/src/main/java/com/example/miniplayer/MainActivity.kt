package com.example.miniplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: Button
    private var currentUri: Uri? = null

    // 系统文件选择器，只显示视频类型，不需要任何存储权限
    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            currentUri = it
            videoView.setVideoURI(it)
            videoView.start()
            btnPlayPause.text = "暂停"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        findViewById<Button>(R.id.btnChoose).setOnClickListener {
            // 只筛选视频文件
            pickVideo.launch(arrayOf("video/*"))
        }

        btnPlayPause.setOnClickListener {
            if (currentUri == null) return@setOnClickListener
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = "播放"
            } else {
                videoView.start()
                btnPlayPause.text = "暂停"
            }
        }

        // 播放完成时把按钮文字恢复为“播放”
        videoView.setOnCompletionListener {
            btnPlayPause.text = "播放"
        }
    }
}
