package com.example.miniplayer

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private lateinit var videoView: CropVideoView
    private lateinit var btnPlayPause: Button
    private lateinit var seekBar: SeekBar
    private lateinit var buttonBar: LinearLayout
    private lateinit var progressRow: LinearLayout
    private var currentUri: Uri? = null
    private var isFullscreen = false
    private var isDragging = false
    private var pendingPosition = 0

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("player", MODE_PRIVATE) }

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
            try {
                contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            playUri(it, 0)
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

        videoView.setOnPreparedListener {
            if (pendingPosition > 0) {
                videoView.seekTo(pendingPosition)
                pendingPosition = 0
            }
            // 视频尺寸就绪后重新测量，让全屏铺满立刻生效
            videoView.requestLayout()
        }

        videoView.setOnCompletionListener {
            btnPlayPause.text = "播放"
            seekBar.progress = 0
            videoView.seekTo(0)
            prefs.edit().putInt("position", 0).apply()
        }

        videoView.setOnErrorListener { _, _, _ ->
            currentUri = null
            prefs.edit().remove("uri").remove("position").apply()
            btnPlayPause.text = "播放"
            seekBar.max = 1
            seekBar.progress = 0
            true
        }

        prefs.getString("uri", null)?.let { saved ->
            val uri = Uri.parse(saved)
            currentUri = uri
            pendingPosition = prefs.getInt("position", 0)
            videoView.setVideoURI(uri)
            btnPlayPause.text = "播放"
        }
    }

    private fun playUri(uri: Uri, startPosition: Int) {
        currentUri = uri
        isDragging = false
        seekBar.max = 1
        seekBar.progress = 0
        pendingPosition = startPosition
        prefs.edit()
            .putString("uri", uri.toString())
            .putInt("position", startPosition)
            .apply()
        videoView.setVideoURI(uri)
        videoView.start()
        btnPlayPause.text = "暂停"
    }

    override fun onStart() {
        super.onStart()
        handler.post(progressUpdater)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(progressUpdater)
        if (currentUri != null) {
            prefs.edit()
                .putString("uri", currentUri.toString())
                .putInt("position", videoView.currentPosition)
                .apply()
        }
        if (videoView.isPlaying) {
            videoView.pause()
            btnPlayPause.text = "播放"
        }
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            buttonBar.visibility = View.GONE
            progressRow.visibility = View.GONE
            // 全屏开启裁剪铺满
            videoView.cropEnabled = true
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            buttonBar.visibility = View.VISIBLE
            progressRow.visibility = View.VISIBLE
            // 退出全屏恢复原始比例（有黑边但完整）
            videoView.cropEnabled = false
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
