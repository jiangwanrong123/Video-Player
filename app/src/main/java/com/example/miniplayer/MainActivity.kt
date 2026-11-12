package com.example.miniplayer

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: Button
    private lateinit var buttonBar: LinearLayout
    private var currentUri: Uri? = null
    private var isFullscreen = false

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
        buttonBar = findViewById(R.id.buttonBar)

        findViewById<Button>(R.id.btnChoose).setOnClickListener {
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

        findViewById<Button>(R.id.btnFullscreen).setOnClickListener {
            toggleFullscreen()
        }

        // 全屏时点击画面退出全屏
        videoView.setOnClickListener {
            if (isFullscreen) toggleFullscreen()
        }

        videoView.setOnCompletionListener {
            btnPlayPause.text = "播放"
        }
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            buttonBar.visibility = View.GONE
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            buttonBar.visibility = View.VISIBLE
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
