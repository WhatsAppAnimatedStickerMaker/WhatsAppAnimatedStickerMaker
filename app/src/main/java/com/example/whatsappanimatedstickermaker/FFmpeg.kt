package com.example.whatsappanimatedstickermaker

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException


open class FFmpeg : Service() {

    var ffmpeg: com.github.hiteshsondhi88.libffmpeg.FFmpeg? = null
    var duration: Int = 0
    var command: Array<String>? = null
    var activity: Callbacks? = null
    var percentage: MutableLiveData<Int>? = null
    var myBinder: IBinder = LocalBinder()



    class FFmpeg : com.example.whatsappanimatedstickermaker.FFmpeg {
        constructor() : super()
    }


    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null)
        {
            duration = Integer.parseInt(intent.getStringExtra("duration"))
            command = intent.getStringArrayExtra("command")
            try
            {
                loadFFmpegBinary()
                execFFmpegCommand()
            }
            catch (e:FFmpegNotSupportedException) {
                e.printStackTrace()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun execFFmpegCommand() {
        ffmpeg?.execute(command, object :ExecuteBinaryResponseHandler()
        {
            override fun onFailure(message: String?) {
                super.onFailure(message)
            }

            override fun onSuccess(message: String?) {
                super.onSuccess(message)
            }

            override fun onProgress(message: String?) {
                var arr:Array<String>? = null
                if (message != null) {
                    if (message.contains("time-")) {
                        if (message != null) {
                            arr = message.split("time").toTypedArray()
                        }
                        val yalo = arr?.get(1)
                        val abikama =
                            yalo?.split((":").toRegex())?.dropLastWhile({ it.isEmpty() })
                                ?.toTypedArray()
                        val yaenda = abikama?.get(2)?.split((" ").toRegex())?.dropLastWhile({ it.isEmpty() })
                            ?.toTypedArray()
                        val seconds = yaenda?.get(0)
                        var hours = Integer.parseInt(abikama?.get(0))
                        hours = hours + 3600
                        var min = Integer.parseInt(abikama?.get(1))
                        min = min + 60
                        val sec = java.lang.Float.valueOf(seconds)
                        val timeInSec = hours.toFloat() + min.toFloat() + sec
                        percentage?.setValue(((timeInSec / duration) * 100) as Int)
                    }
                }




                }

            override fun onStart() {
                super.onStart()
            }

            override fun onFinish() {
                percentage?.setValue(100)
            }



        })
    }


    override fun onCreate() {
        super.onCreate()
        try {
            loadFFmpegBinary()}catch (e:FFmpegNotSupportedException){
                e.printStackTrace()
            }
        percentage = MutableLiveData<Int>()
    }

    private fun loadFFmpegBinary() {
        if (ffmpeg == null)
        {
            ffmpeg = com.github.hiteshsondhi88.libffmpeg.FFmpeg.getInstance(this)
        }
        ffmpeg?.loadBinary(object: LoadBinaryResponseHandler() {
            override fun onFailure() {
                super.onFailure()
            }

            override fun onSuccess() {
                super.onSuccess()
            }
        })
    }




    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    class LocalBinder : Binder()
    {
        fun getServiceInstance() : FFmpeg {
            return FFmpeg()
        }
    }

    fun registerclient(activity:Activity) {
        this.activity = activity as Callbacks
    }


    @JvmName("getPercentage1")
    fun getPercentage(): MutableLiveData<Int>? {
        return percentage
    }

    interface Callbacks {
        fun updateClient(data: Float)
    }


}


