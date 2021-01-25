package com.example.whatsappanimatedstickermaker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dinuscxj.progressbar.CircleProgressBar
import kotlinx.android.synthetic.main.activity_progress_bar.*

class ProgressBarActivity : AppCompatActivity() {

    var circleprogressbar: CircleProgressBar? = null
    var duration:Int?=null
    var command:Array<String>? = null
    var path:String?= null
    var mConnection: ServiceConnection?=null
    var ffMpegservice: FFmpeg? = null
    var res:Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)

        circleprogressbar = findViewById(R.id.circleProgressBar)
        circleprogressbar?.setMax(100)

        val i : Intent = getIntent()

        if(i!=null)
        {
            duration = i.getIntExtra("duration", 0)
            command = i.getStringArrayExtra("command")
            path = i.getStringExtra("destination")
            val myIntent = Intent(this@ProgressBarActivity, FFmpeg::class.java)
            myIntent.putExtra("duration", duration.toString())
            myIntent.putExtra("command", command)
            myIntent.putExtra("destination", path)
            startService(myIntent)

            mConnection = object: ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as FFmpeg.LocalBinder
                    ffMpegservice = binder.getServiceInstance()
                    (ffMpegservice as FFmpeg.FFmpeg).registerclient(parent)

                    val resultObserver: Observer<Int> = object:Observer<Int> {
                        override fun onChanged(t: Int?) {
                            res = t
                            if (res!! < 100)
                            {
                                circleProgressBar.setProgress(res!!)
                            }
                            if (res === 100)
                            {
                                circleProgressBar.setProgress(res!!)
                                stopService(myIntent)
                                Toast.makeText(getApplicationContext(), "Video trimmed successfully", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    TODO("Not yet implemented")
                }

            }

            bindService(myIntent, mConnection as ServiceConnection,Context.BIND_AUTO_CREATE)

        }










        }
}