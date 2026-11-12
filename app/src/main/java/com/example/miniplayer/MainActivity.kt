package com.example.miniplayer

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: Button
    private lateinit var seekBar: SeekBar
    private lateinit var buttonBar: LinearLayout
    private lateinit var progressRow: LinearLayout
    private var currentUri: Uri? = null
    private var isFullscreen = false
    private var isDragging = false

    private val handler = Handler(Looper.getMainLooper())

    // 每 500ms 把播放位置同步到进度条
    private val progressUpdater = object : Runnable {
        override fun run() {
            if (currentUri != null && !isDragging) {
                val duration = videoView.duration
                if (duration > 0) {
                    seekBar.max = duration
                    seekBar.progress = videoView.currentPosition
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            currentUri = it
            isDragging = false
            seekBar.max = 1
            seekBar.progress = 0
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
        seekBar = findViewById(R.id.seekBar)
        buttonBar = findViewById(R.id.buttonBar)
        progressRow = findViewById(R.id.progressRow)

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

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) videoView.seekTo(progress)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                isDragging = true
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                isDragging = false
            }
        })

        findViewById<Button>(R.id.btnFullscreen).setOnClickListener {
            toggleFullscreen()
        }

        videoView.setOnClickListener {
            if (isFullscreen) toggleFullscreen()
        }

        videoView.setOnCompletionListener {
            btnPlayPause.text = "播放"
            seekBar.progress = 0
            videoView.seekTo(0)
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(progressUpdater)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(progressUpdater)
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
            progressRow.visibility = View.VISIBLE
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
