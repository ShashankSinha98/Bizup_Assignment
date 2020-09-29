package com.example.bizupassignment

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bizupassignment.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var listVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val recyclerView = background_list


        binding.changeBackground.setOnClickListener {
            if (!listVisible) {
                recyclerView.visibility = View.VISIBLE
                listVisible = true
            } else {
                recyclerView.visibility = View.GONE
                listVisible = false
            }
        }


        val list = arrayOf(
            R.drawable.bg1,
            R.drawable.bg2,
            R.drawable.bg3,
            R.drawable.bg4,
            R.drawable.bg5,
            R.drawable.bg6,
            R.drawable.bg7,
            R.drawable.bg8,
            R.drawable.bg9,
            R.drawable.bg10
        )

        var adapter =
            BackgroundAdapter(list, R.drawable.bg1, object : BackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    binding.backgroundImg.setImageResource(list[position])
                }
            })

        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()



        binding.shareBtn.setOnClickListener {
            //generateImage()
            binding.backgroundList.visibility = View.GONE
            listVisible = false
            binding.changeBackground.visibility = View.INVISIBLE
            binding.textEt.isCursorVisible = false
            generateScreenShot(binding.relativeLayout)
        }
    }




    private fun generateScreenShot(view: View) {

        // Hide keyboard
        val inm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(view.windowToken, 0)

        binding.shareBtn.visibility = View.GONE

        val bitmap = Bitmap.createBitmap(
            view.getWidth(),
            view.getHeight(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        try {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                FileOutputStream(File("mnt/sdcard/MySS.jpg"))
            )

            shareImage(bitmap)
        } catch (e: Exception) {
            Timber.i(e.message.toString())
        }

        binding.shareBtn.visibility = View.VISIBLE
        binding.changeBackground.visibility = View.INVISIBLE
        binding.textEt.isCursorVisible = true

    }


    // Sharing Image via Intent
    private fun shareImage(bitmap: Bitmap) {
        try {

            // Saving Image

            val cachePath: File = File(this.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

        } catch (e: IOException) {
            Timber.i(e.message.toString())
            e.printStackTrace()
        }

        val imagePath = File(this.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri =
            FileProvider.getUriForFile(
                this,
                "com.example.bizupassignment.fileprovider",
                newFile
            )

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }

    }
}

