package com.example.bizupassignment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bizupassignment.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val PROCESS_WAIT: Long = 300
const val STORAGE_REQUEST_CODE = 100

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var listVisible = false
    private var colorPos = 0
    private var fontPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // List of all permissions
        val permissions: Array<String> = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        // Requesting Permission
        ActivityCompat.requestPermissions(this, permissions, STORAGE_REQUEST_CODE)

        // List of all color options
        val colors = arrayOf(
            R.color.white,
            R.color.red,
            R.color.colorAccent,
            R.color.dark_green,
            R.color.yellow,
            R.color.violet,
            R.color.green,
            R.color.pink,
            R.color.orange
        )

        // List of all background options
        val backgrounds = arrayOf(
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

        // List of all font options
        val fonts = arrayOf(
            R.font.font1,
            R.font.font2,
            R.font.font3,
            R.font.font4,
            R.font.font5
        )


        // Data Binding Implementation
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Change Color Functionality
        binding.changeColorIcon.setOnClickListener {
            colorPos += 1
            val colorSelected = colors[colorPos % colors.size]

            binding.apply {
                textEt.setTextColor(resources.getColor(colorSelected))
                statusTv.setTextColor(resources.getColor(colorSelected))
                notifyChange()
            }
            DrawableCompat.setTint(
                binding.changeColorIcon.drawable,
                ContextCompat.getColor(this, colorSelected)
            )
        }

        // Change Background Functionality
        binding.changeBackground.setOnClickListener {
            if (!listVisible) {
                binding.backgroundList.visibility = View.VISIBLE
                listVisible = true
            } else {
                binding.backgroundList.visibility = View.GONE
                listVisible = false
            }
        }

        // Change Font Style Functionality
        binding.changeFontIcon.setOnClickListener {
            fontPos += 1
            val fontSelected = fonts[fontPos % fonts.size]
            val typeface = ResourcesCompat.getFont(this, fontSelected)

            binding.apply {
                textEt.typeface = typeface
                statusTv.typeface = typeface
                notifyChange()
            }
        }

        // Implemented change background via interface
        val adapter =
            BackgroundAdapter(
                backgrounds,
                R.drawable.bg1,
                object : BackgroundAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        binding.backgroundImg.setImageResource(backgrounds[position])
                    }
                })

        // Adapter initial set up
        setUpRecyclerView(adapter)


        // Share Functionality
        binding.shareBtn.setOnClickListener {

            // Checking for permissions
            if (hasPermissions(this, permissions)) {

                // Permission Granted
                listVisible = false
                binding.apply {
                    backgroundList.visibility = View.GONE
                    changeBackground.visibility = View.INVISIBLE
                    textEt.visibility = View.INVISIBLE
                    statusTv.visibility = View.VISIBLE
                    statusTv.text = binding.textEt.text?.toString()
                    changeColorIcon.visibility = View.INVISIBLE
                    shareBtn.visibility = View.INVISIBLE
                    changeFontIcon.visibility = View.INVISIBLE

                    invalidateAll()
                }
                hideKeyboard(it)
                Toast.makeText(this, "Generating Image... Please Wait.", Toast.LENGTH_SHORT).show()
                val handler = Handler()
                handler.postDelayed({
                    generateImage(binding.relativeLayout)
                }, PROCESS_WAIT)
            } else {

                // Permission Not Granted
                Toast.makeText(this, getString(R.string.Permission_not_given), Toast.LENGTH_SHORT)
                    .show()

                // Asking for permission again
                ActivityCompat.requestPermissions(this, permissions, STORAGE_REQUEST_CODE)
            }

        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun setUpRecyclerView(adapter: BackgroundAdapter) {
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.apply {
            backgroundList.layoutManager = mLayoutManager
            backgroundList.itemAnimator = DefaultItemAnimator()
            backgroundList.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onDestroy() {
        super.onDestroy()
        DrawableCompat.setTint(
            binding.changeColorIcon.drawable,
            ContextCompat.getColor(this, R.color.white)
        )
    }

    private fun generateImage(view: View) {

        binding.shareBtn.visibility = View.GONE

        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
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

        binding.apply {
            shareBtn.visibility = View.VISIBLE
            changeBackground.visibility = View.VISIBLE
            textEt.visibility = View.VISIBLE
            statusTv.visibility = View.INVISIBLE
            changeColorIcon.visibility = View.VISIBLE
            changeFontIcon.visibility = View.VISIBLE
            shareBtn.visibility = View.VISIBLE
        }


    }

    override fun onRestart() {
        super.onRestart()
        binding.apply {
            shareBtn.visibility = View.VISIBLE
            changeBackground.visibility = View.VISIBLE
            textEt.visibility = View.VISIBLE
            statusTv.visibility = View.INVISIBLE
            changeFontIcon.visibility = View.VISIBLE
            changeColorIcon.visibility = View.VISIBLE
            shareBtn.visibility = View.VISIBLE
        }
    }

    // Sharing Image via Intent
    private fun shareImage(bitmap: Bitmap) {
        try {

            // Saving Image

            val cachePath = File(this.cacheDir, "images")
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

            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Choose an app"))
            } else {
                Toast.makeText(this, "No app found to share image", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

