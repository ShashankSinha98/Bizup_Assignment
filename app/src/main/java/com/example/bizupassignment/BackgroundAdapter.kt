package com.example.bizupassignment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class BackgroundAdapter(private val listItem: Array<Int>, private var current: Int, val btnClickListener: BtnClickListener):
        RecyclerView.Adapter<BackgroundAdapter.MyViewHolder>(){

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var card_image: ImageView = view.findViewById(R.id.custom_bg_iv)
        var card: CardView = view.findViewById(R.id.custom_img_card)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_view_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        mClickListener = btnClickListener
        var params = holder.card.layoutParams as ViewGroup.MarginLayoutParams

        if(listItem.get(position) == current){
            params.setMargins(10, 10, 10, 10)
        } else {
            params.setMargins(0, 0, 0, 0)
        }

        holder.card.requestLayout()
        holder.card_image.setImageResource(listItem.get(position))

        holder.card.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if(mClickListener != null && listItem[position] != current){
                    current = listItem[position]
                    notifyDataSetChanged()
                    mClickListener?.onBtnClick(position)
                }
            }
        })
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }


    override fun getItemCount() = listItem.size
}