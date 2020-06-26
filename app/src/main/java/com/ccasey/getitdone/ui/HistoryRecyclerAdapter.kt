package com.ccasey.getitdone.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ccasey.getitdone.R
import com.ccasey.getitdone.model.Run

class HistoryRecyclerAdapter(private val runList: List<Run>): RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_run,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.distance.text = runList[position].distance.toString()
        holder.duration.text = runList[position].duration.toString()
        holder.mileSplit.text = runList[position].mileSplit.toString()
        holder.dateTime.text = runList[position].dateTime.toString()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var duration: TextView = itemView.findViewById(R.id.listItemDurationNum)
        var distance: TextView = itemView.findViewById(R.id.listItemDistanceNum)
        var mileSplit: TextView = itemView.findViewById(R.id.listItemPaceNum)
        var dateTime: TextView = itemView.findViewById(R.id.listItemDateValue)
        var routeImage: ImageView = itemView.findViewById(R.id.routeImg)
    }
}