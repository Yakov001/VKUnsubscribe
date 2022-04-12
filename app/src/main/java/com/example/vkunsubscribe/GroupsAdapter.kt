package com.example.vkunsubscribe

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class GroupsAdapter(
    private var dataSet: MutableList<GroupsGroupFull> = mutableListOf(),
    private val listener: OnGroupSelectedListener
) :
    RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

    private var selectedGroups: MutableList<GroupsGroupFull> = mutableListOf()

    interface OnGroupSelectedListener {
        fun onGroupSelected(group: GroupsGroupFull)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupId : TextView = view.findViewById(R.id.group_id)
        val draweeView : SimpleDraweeView = view.findViewById(R.id.group_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View? = null
        when (viewType) {
            0 -> view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item, parent, false)
            1 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_item_selected, parent, false)
                view.isClickable = true
            }
        }
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        dataSet.let {
            holder.groupId.text = it.get(position).name
            val uri = Uri.parse(it.get(position).photo200)
            holder.draweeView.setImageURI(uri)
        }

        holder.itemView.setOnClickListener {
            dataSet.get(position).let { listener.onGroupSelected(it) }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.count()
    }

    fun setData(dataSet: MutableList<GroupsGroupFull>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (selectedGroups.contains(dataSet[position])) 1 else 0
    }

    fun updateItem(group: GroupsGroupFull) {
        notifyItemChanged(selectedGroups.indexOf(group))
    }

    fun updateSelectedGroups(selectedGroups: MutableList<GroupsGroupFull>) {
        this.selectedGroups = selectedGroups
    }

}