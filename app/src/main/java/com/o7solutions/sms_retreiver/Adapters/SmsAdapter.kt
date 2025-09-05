package com.o7solutions.sms_retreiver.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.sms_retreiver.R
import com.o7solutions.sms_retreiver.data_classes.SmsMessage

class SmsAdapter(private val smsList: ArrayList<SmsMessage>) :
    RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    inner class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fromText: TextView = itemView.findViewById(R.id.tvFrom)
        val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        val typeText: TextView = itemView.findViewById(R.id.tvType)
        val dateText: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = smsList[position]
        holder.fromText.text = sms.from
        holder.messageText.text = sms.message
        holder.typeText.text = sms.type
        holder.dateText.text = sms.date
    }

    override fun getItemCount(): Int = smsList.size
}
