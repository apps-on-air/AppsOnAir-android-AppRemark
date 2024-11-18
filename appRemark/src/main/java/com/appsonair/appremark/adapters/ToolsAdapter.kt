package com.appsonair.appremark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.appremark.R
import com.appsonair.appremark.enums.ToolType

class ToolsAdapter(private val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {
    private val mToolList: MutableList<ToolModel> = ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    internal inner class ToolModel(
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgToolIcon: ImageView = itemView.findViewById(R.id.img_tool)

        init {
            itemView.setOnClickListener { _: View? ->
                mOnItemSelected.onToolSelected(
                    mToolList[layoutPosition].mToolType
                )
            }
        }
    }

    init {
        mToolList.add(ToolModel(R.drawable.ic_oval, ToolType.SHAPE))
        mToolList.add(ToolModel(R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(ToolModel(R.drawable.ic_eraser, ToolType.ERASER))
        mToolList.add(ToolModel(R.drawable.ic_undo, ToolType.UNDO))
        mToolList.add(ToolModel(R.drawable.ic_redo, ToolType.REDO))
//        uncomment below code to add emoji on photo & pick image from gallery
//        mToolList.add(ToolModel(R.drawable.ic_emoji, ToolType.EMOJI))
//        mToolList.add(ToolModel(R.drawable.ic_gallery, ToolType.GALLERY))
    }
}