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

    lateinit var binding : ActivityMainBinding

    private var listVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
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


        val list = arrayOf(R.drawable.bg1,R.drawable.bg2,
            R.drawable.bg3,R.drawable.bg4,R.drawable.bg5, R.drawable.bg6,R.drawable.bg7,R.drawable.bg8,R.drawable.bg9,R.drawable.bg10)

        var adapter = BackgroundAdapter(list,R.drawable.bg1, object : BackgroundAdapter.BtnClickListener{
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

        /*val viewAdapter = MyAdapter(arrayOf(R.drawable.bg1,R.drawable.bg1,R.drawable.bg1))

        binding.backgroundList.run {
            setHasFixedSize(true)
            adapter = viewAdapter
            Log.i("xlr8_c",viewAdapter.itemCount.toString())
            invalidate()
        }*/


        binding.shareBtn.setOnClickListener {
            //generateImage()
            binding.backgroundList.visibility = View.GONE
            listVisible = false
            binding.changeBackground.visibility = View.INVISIBLE
            binding.textEt.isCursorVisible = false
            generateScreenShot(binding.relativeLayout)
        }
    }



    /*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.add_img -> {
                Toast.makeText(this,"Add Img",Toast.LENGTH_SHORT).show()
                return true
            }

        }

        return super.onOptionsItemSelected(item)

    }
    */

    private fun generateScreenShot(view : View) {

        // Hide keyboard
        val inm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(view.windowToken,0)

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
                FileOutputStream(File("mnt/sdcard/MySS.jpg")))

            shareImage(bitmap)
        } catch (e :Exception){
            Log.i("xlr8_txt_err",e.message.toString())
        }

        binding.shareBtn.visibility = View.VISIBLE
        binding.changeBackground.visibility = View.INVISIBLE
        binding.textEt.isCursorVisible = true

    }


    // Sharing Image via Intent
    private fun shareImage(bitmap: Bitmap){
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


    // Generating Image
    fun generateImage(){
        //val src = BitmapFactory.decodeResource(resources,R.drawable.bg1)

        var bitmap  = BitmapFactory.decodeResource(resources, R.drawable.bg1)
        bitmap = getResizedBitmap(bitmap, getScreenWidth(), getScreenHeight())

        val dest : Bitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )


        Timber.i("img width- ${bitmap.width}, img height- ${bitmap.height} ")
        Timber.i("phone width - ${getScreenWidth()}, phone height- ${getScreenHeight()}")

        val text  = text_et.text.toString()

        val cs : Canvas = Canvas(dest)
        val tpaint : Paint = Paint()
        tpaint.textSize = 35f
        tpaint.color = Color.WHITE
        tpaint.style = Paint.Style.FILL



        cs.drawBitmap(bitmap, 0f, 0f, null)
        val height = tpaint.measureText("yY")
        val width = tpaint.measureText(text)

        val xCoord = (bitmap.width - width)/2
        val yCoord = (bitmap.height - height)/2
        //cs.drawText(text, xCoord, yCoord, tpaint)
        Log.i("xlr8_txt", text)

        val strArr = text.split("\\n+".toRegex()).toTypedArray()
        val txtLen = text.length

        var count = 0
        strArr.forEach {
            Log.i("xlr8_txt", it)
            count+=1
        }

        Log.i("xlr8_txt", "len : ${txtLen}, new Lines : ${count} ")

        try{
            shareImage(dest)
            /*dest.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                FileOutputStream(File("mnt/sdcard/ImageAfterAddingText.jpg"))
            )*/
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show()
            Timber.i("File Saved")

        } catch (e: FileNotFoundException){
            Timber.i(e.message.toString())
            Toast.makeText(this, "Saving Error", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }


    // Getting Device Screen Size
    private fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels
    private fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels


    // Resizing Image
    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }


}

/*
class MyAdapter(private val myDataSet : Array<Int>):
    RecyclerView.Adapter<MyAdapter.ViewHolder>(){

    class ViewHolder(val item : View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_view_item,parent,false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.findViewById<ImageView>(R.id.card_img)
            .setImageResource(list_of_background[position])
    }

    override fun getItemCount() = myDataSet.size
}


private val list_of_background = listOf(R.drawable.bg1,R.drawable.bg1,R.drawable.bg1)*/