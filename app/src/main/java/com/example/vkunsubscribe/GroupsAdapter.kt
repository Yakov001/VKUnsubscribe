package com.example.vkunsubscribe

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class GroupsAdapter(private var dataSet: GroupsGetObjectExtendedResponse? = null) :
    RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupId : TextView = view.findViewById(R.id.group_id)
        val draweeView : SimpleDraweeView = view.findViewById(R.id.group_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.groupId.text = dataSet?.items?.get(position).toString()

        dataSet?.let {
            holder.groupId.text = it.items.get(position).name
            val uri = Uri.parse(it.items.get(position).photo200)
            holder.draweeView.setImageURI(uri)
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.count ?: 0
    }

    fun setData(dataSet: GroupsGetObjectExtendedResponse) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

}