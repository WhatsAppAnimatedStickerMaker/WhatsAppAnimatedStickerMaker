package com.example.whatsappanimatedstickermaker

import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.florescu.android.rangeseekbar.RangeSeekBar
import java.io.File


class TrimActivity : AppCompatActivity() {
    var uri: Uri? = null
    var imageview: ImageView? = null
    var videoView2: VideoView? = null
    var textViewleft: TextView? = null
    var textViewRight: TextView? = null
    lateinit var rangeseekbar: RangeSeekBar<Int>
    var duration: Int = 0
    var filePrefix: String? = null
    var command: Array<String>? = null
    var dest: File? = null
    var original_path: String? = null
    var isPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim)
        imageview = findViewById(R.id.pause);
        videoView2 = findViewById(R.id.vv2);
        textViewleft = findViewById(R.id.tvvLeft);
        textViewRight = findViewById(R.id.tvvRight);
        rangeseekbar = findViewById(R.id.seekbar);


        val i = intent

        if (i != null) {
            var imgPath = i.getStringExtra("uri")
            var videopath = imgPath?.let { String.format(it,".mp4") }
            uri = Uri.parse(videopath)
            isPlaying = true
            Log.d("grrr", imgPath!!)
            videoView2?.setVideoURI(uri)
            videoView2?.requestFocus()
            videoView2?.start()
        }
        setListeners()
    }






    private fun setListeners() {
        imageview?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (isPlaying) {
                    imageview?.setImageResource(R.drawable.ic_play)
                    videoView2?.pause()
                    isPlaying = false
                } else {
                    videoView2?.start()
                    imageview?.setImageResource(R.drawable.ic_pause)
                    isPlaying = true
                }
            }
        })

        videoView2?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {


            override fun onPrepared(mp: MediaPlayer?) {
                videoView2?.start()
                duration = mp?.duration?.div(1000)!!
                textViewleft?.text = "00:00:00"
                textViewRight?.text = getTime(mp.duration.div(1000))
                mp.isLooping = true
                rangeseekbar.setRangeValues(0, duration)
                rangeseekbar.selectedMaxValue = duration
                rangeseekbar.selectedMinValue = 0
                rangeseekbar.isEnabled = true


                rangeseekbar.setOnRangeSeekBarChangeListener(object : RangeSeekBar.OnRangeSeekBarChangeListener<Int> {

                    override fun onRangeSeekBarValuesChanged(
                        bar: RangeSeekBar<*>?,
                        minValue: Int?,
                        maxValue: Int?
                    ) {
                        videoView2!!.seekTo(minValue as Int * 1000)

                        if (bar != null) {
                            textViewleft?.text = getTime(bar.selectedMinValue as Int)
                        }
                        if (bar != null) {
                            textViewRight?.text = getTime(bar.selectedMaxValue as Int)
                        }
                    }


                })


                val handler = Handler()
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        if (videoView2?.currentPosition!! >= rangeseekbar.selectedMaxValue
                                .toInt() * 1000
                        )
                            videoView2?.seekTo(rangeseekbar.selectedMinValue.toInt() * 1000)
                    }
                }, 1000)

            }
        })

    }

    private fun getTime(seconds: Int): String {
        val hr = seconds / 3600
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d",hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.trim) {
            val alert = AlertDialog.Builder(this@TrimActivity)
            val linearLayout = LinearLayout(this@TrimActivity)
            linearLayout.orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(50, 0, 50, 100)
            val input = EditText(this@TrimActivity)
            input.layoutParams = lp
            input.gravity = Gravity.TOP or Gravity.START
            input.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            linearLayout.addView(input, lp)

            alert.setMessage("Set video name?")
            alert.setTitle("Change video name?")
            alert.setView(linearLayout)
            alert.setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialogInterface:DialogInterface, i:Int) {
                    dialogInterface.dismiss()
                }
            })
            alert.setPositiveButton("Submit", object: DialogInterface.OnClickListener {
                override fun onClick(dialogInterface:DialogInterface, i:Int) {
                    filePrefix = input.text.toString()
                    trimVideo(rangeseekbar.selectedMinValue.toInt() * 1000, rangeseekbar.selectedMaxValue.toInt() * 1000,
                        filePrefix!!
                    )

                    val myintent = Intent(this@TrimActivity, ProgressBarActivity::class.java)
                    myintent.putExtra("duration", duration)
                    myintent.putExtra("command", command)
                    myintent.putExtra("destination", dest?.absolutePath)
                    startActivity(myintent)
                    finish()
                    dialogInterface.dismiss()
                    alert.show()
                    
                }
            })
        }

        return super.onOptionsItemSelected(item)
    }

    private fun trimVideo(startMs: Int, endMs: Int, fileName: String) {

        val folder = File(Environment.getExternalStorageDirectory() , "/TrimVideos")
        if (!folder.exists())
        {
            folder.mkdir()
        }
        filePrefix = fileName
        val fileExt = ".mp4"
        dest = File(folder, filePrefix + fileExt)
        original_path = getRealPathFromUri(uri)

        duration = (endMs - startMs) / 1000
        command = original_path?.let {
            dest?.let { it1 ->
                arrayOf<String>("-ss", "" + startMs / 1000, "-y", "-i",
                    it, "-t", "" + (endMs - startMs) / 1000, "-vcodec", ".mp4", "-b:v", "2097152", "-b:a", "48000", "-ac", "22050", it1.absolutePath
                )
            }
        }

    }

    private fun getRealPathFromUri( contentUri: Uri?): String? {
        var cursor: Cursor?



            val proj = arrayOf<String>(MediaStore.Video.Media.DATA)
            cursor = contentUri?.let { contentResolver.query(it,proj,null,null,null) }
        return if(cursor!=null){
            val coloumn_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(coloumn_index)
        }else
            null
            }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

}









