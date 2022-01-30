package com.task.mobiadmin.fragment.home

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatSeekBar
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.SeekBar
import com.task.mobi.R
import com.task.mobi.activity.BaseActivity
import com.task.mobi.customViews.MyTextView
import com.task.mobi.utils.PermissionsManager
import com.task.mobi.utils.Utils
import java.io.File
import java.util.*

/**
 * Created by neeraj on 21/4/18.
 */
class TaskRecorderAndPlayer(private val baseActivity: BaseActivity,
                            private val recordingTimeTV: MyTextView,
                            private val firstCL: ConstraintLayout,
                            private val elapseTV: MyTextView,
                            private val totalTV: MyTextView,
                            private val progressSB: AppCompatSeekBar,
                            private val recordPlayPauseIBT: ImageButton,
                            private val stopIBT: ImageButton,
                            private val closeIBT: ImageButton,
                            private val onRecorderCallbacks: RecorderCallbacks) {

    val IDLE = 1
    val RECORDING = 2
    val REC_STOP = 3
    val PLAY = 4
    val PAUSE = 5
    val PLAY_STOP = 6
    var audioRecorder: MediaRecorder? = MediaRecorder()
    private lateinit var file: String
    var playerMode = IDLE
    var count = 0
    var timer = Timer()
    val player = MediaPlayer()
    private var timerTask: TimerTask? = null
    private var showRemoveFile: Boolean = true

    init {
        updateUI(IDLE)
        recordPlayPauseIBT.setOnClickListener({
            when (playerMode) {
                IDLE ->
                    if (PermissionsManager.checkPermissions(baseActivity,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            12,
                            object : PermissionsManager.PermissionCallback {
                                override fun onPermissionsGranted(resultCode: Int) {
                                    startMediaRecorder()
                                    startRecordingCounter()
                                    updateUI(RECORDING)
                                }

                                override fun onPermissionsDenied(resultCode: Int) {
                                }

                            })) {
                        startMediaRecorder()
                        startRecordingCounter()
                        updateUI(RECORDING)
                    }
                REC_STOP -> {
                    player.start()
                    updateUI(PLAY)
                    startPlayCounter()
                }
                PLAY -> {
                    player.pause()
                    updateUI(PAUSE)
                }
                PAUSE -> {
                    player.start()
                    updateUI(PLAY)
                }
                PLAY_STOP -> {
                    player.start()
                    updateUI(PLAY)
                    startPlayCounter()
                }

            }
        })
        stopIBT.setOnClickListener({
            when (playerMode) {
                RECORDING -> {
                    if (count < 5) {
                        baseActivity.showToast("Minimum recording time is 5 sec", false)
                    } else {
                        stopRecording()
                        updateUI(REC_STOP)
                    }
                }
                PLAY, PAUSE -> {
                    stopPlayer()
                    player.prepare()
                    updateUI(PLAY_STOP)
                }
            }
        })
        closeIBT.setOnClickListener({
            if (showRemoveFile) {
                val bldr = AlertDialog.Builder(baseActivity)
                bldr.setTitle("Remove Voice Clip")
                bldr.setMessage("Are you sure want to remove Voice Clip?")
                bldr.setPositiveButton("Yes", { dialog, which -> closeRecorder() })
                bldr.setNegativeButton("No", null)
                bldr.create().show()
            } else {
                closeRecorder()
            }
        })
    }

    private fun closeRecorder() {
        if (playerMode == RECORDING) {
            stopRecording()
        } else if (playerMode == PLAY || playerMode == PAUSE) {
            stopPlayer()
        }
        onRecorderCallbacks.onRecorderClose()
    }

    private fun stopPlayer() {
        stopCounter()
        try {
            player.stop()
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun stopRecording() {
        stopCounter()
        try {
            audioRecorder?.stop()
            audioRecorder?.reset()
            audioRecorder?.release()
            audioRecorder = null
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun startMediaRecorder() {
        audioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        audioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        audioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        file = baseActivity.cacheDir.absolutePath + System.currentTimeMillis() + ".3gp"
        audioRecorder?.setOutputFile(file)
        audioRecorder?.setMaxDuration(2 * 60 * 1000)
        audioRecorder?.setOnInfoListener { mr, what, _ ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                baseActivity.showToast("Max Length audio reached", true)
                mr.stop()
                mr.release()
                stopCounter()
                updateUI(REC_STOP)
            }
        }
        audioRecorder?.prepare()
        audioRecorder?.start()
    }

    fun getFile(): File {
        return File(file)
    }

    private fun startRecordingCounter() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                count++
                baseActivity.runOnUiThread({
                    recordingTimeTV.setText(Utils.changeDateFormat(count.toString(), "ss", "mm:ss") + "/04:00")
                })
            }
        }
        timer.scheduleAtFixedRate(timerTask, 1100, 1000)
    }

    private fun startPlayCounter() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                progressSB.progress = player.currentPosition
            }
        }
        timer.scheduleAtFixedRate(timerTask, 100, 100)
    }

    private fun stopCounter() {
        timer.cancel()
        timerTask?.cancel()
    }

    private fun updateUI(mode: Int) {
        playerMode = mode
        log("Mode called ${mode}")
        when (mode) {
            IDLE -> {
                firstCL.visibility = View.GONE
                stopIBT.visibility = View.GONE
                recordPlayPauseIBT.visibility = View.VISIBLE
                recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_record))
                recordingTimeTV.visibility = View.VISIBLE
                count = 0
                recordingTimeTV.setText(Utils.changeDateFormat(count.toString(), "ss", "mm:ss") + "/04:00")
            }
            RECORDING -> {
                recordPlayPauseIBT.visibility = View.GONE
                stopIBT.visibility = View.VISIBLE
            }
            REC_STOP -> {
                val outAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_out_top_center)
                recordingTimeTV.startAnimation(outAnim)
                stopIBT.startAnimation(outAnim)
                outAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        recordingTimeTV.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                val incomingAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_bottom)
                firstCL.startAnimation(incomingAnim)
                recordPlayPauseIBT.startAnimation(incomingAnim)
                stopIBT.startAnimation(incomingAnim)
                firstCL.visibility = View.VISIBLE
                recordPlayPauseIBT.visibility = View.VISIBLE
                recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_play_white))
                preparePlayer(file)
            }
            PLAY -> {
                recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_pause_white))
            }
            PAUSE -> {
                recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_play_white))
            }
            PLAY_STOP -> {
                recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_play_white))
                Handler().postDelayed({
                    elapseTV.setText("00:00")
                }, 100)
                progressSB.progress = 0
            }
        }
    }

    fun createPlayerViewAndLoadFile(file: String) {
        showRemoveFile = false
        playerMode = REC_STOP
        recordingTimeTV.visibility = View.GONE
        firstCL.visibility = View.VISIBLE
        recordPlayPauseIBT.visibility = View.VISIBLE
        stopIBT.visibility = View.VISIBLE
        recordPlayPauseIBT.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_m_play_white))
        preparePlayer(file)
    }

    fun preparePlayer(file: String) {
        player.reset()
        player.setDataSource(file)
        player.prepare()
        progressSB.max = player.duration
        Handler().postDelayed({
            elapseTV.setText("00:00")
            progressSB.progress = 0
        }, 10)
        totalTV.setText(Utils.changeDateFormat((player.duration / 1000).toString(), "ss", "mm:ss"))
        progressSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress)
                }
                elapseTV.setText(Utils.changeDateFormat((player.currentPosition / 1000).toString(), "ss", "mm:ss"))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        player.setOnCompletionListener {
            stopPlayer()
            player.prepare()
            updateUI(PLAY_STOP)
        }
    }

    private fun log(s: String) {
        baseActivity.log(s)
    }

    fun onStop() {
        closeRecorder()
        player.release()
    }

    interface RecorderCallbacks {
        fun onRecorderClose()
    }
}