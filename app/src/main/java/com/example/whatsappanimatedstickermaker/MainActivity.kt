package com.example.whatsappanimatedstickermaker

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var btn: Button? = null
    private var btn2 : Button? = null
    private var videoView: VideoView? = null
    private val VIDEO_DIRECTORY = "/demonuts"
    private val GALLERY: Int = 1
    private var CAMERA: Int = 2
    private var pathsent:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.selbutton) as Button
        btn2 = findViewById(R.id.edit) as Button
        videoView = findViewById(R.id.vv) as VideoView

        btn!!.setOnClickListener { showPictureDialog() }
        btn2!!.setOnClickListener { gotoEditActivity() }



    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf<String>(
            "Select video from gallery",
            "Record video from camera"
        )
        pictureDialog.setItems(pictureDialogItems,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    when (which) {
                        0 -> chooseVideoFromGallary()
                        1 -> takeVideoFromCamera()
                    }
                }
            })
        pictureDialog.show()
    }

    fun chooseVideoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("result", "" + resultCode)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("what", "cancel")
            return
        }
        if (requestCode == GALLERY) {
            Log.d("what", "gale")
            if (data != null) {
                val contentURI = data.data
                val selectedVideoPath = getPath(contentURI!!)
                Log.d("path", selectedVideoPath!!)
                saveVideoToInternalStorage(selectedVideoPath)
                videoView?.setVideoURI(contentURI)
                videoView?.requestFocus()
                videoView?.start()
                pathsent = selectedVideoPath.toString()


            }
        } else if (requestCode == CAMERA) {
            val contentURI = data!!.data
            val recordedVideoPath = getPath(contentURI!!)
            Log.d("frrr", recordedVideoPath!!)
            saveVideoToInternalStorage(recordedVideoPath)
            videoView?.setVideoURI(contentURI)
            videoView?.requestFocus()
            videoView?.start()
            pathsent = recordedVideoPath.toString()

        }


    }

    private fun gotoEditActivity() {

         val i = Intent(this@MainActivity, TrimActivity::class.java)
            i.putExtra("uri", pathsent.toString())
            startActivity(i)


    }





    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveVideoToInternalStorage(filePath: String) {
        val newfile: File
        try {
            val currentFile = File(filePath)
            val wallpaperDirectory =
                File(Environment.getExternalStorageDirectory().toString() + VIDEO_DIRECTORY)
            newfile = File(
                wallpaperDirectory,
                Calendar.getInstance().timeInMillis.toString() + ".mp4"
            )
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs()
            }
            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newfile)


                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
                Log.v("vii", "Video file saved successfully.")
            } else {
                Log.v("vii", "Video saving failed. Source file missing.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPath(uri: Uri): String? {
        val projection = arrayOf<String>(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else
            null
    }


}