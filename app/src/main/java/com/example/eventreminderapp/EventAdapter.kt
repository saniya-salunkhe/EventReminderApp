package com.example.eventreminderapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventreminderapp.databinding.ItemEventBinding

class EventAdapter(
    private var events: MutableList<Event>,
    private val listener: EventClickListener
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    interface EventClickListener {
        fun onEventClick(event: Event)
        fun onEventLongClick(event: Event)
    }

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.binding.tvTitle.text = event.title
        holder.binding.tvDescription.text = event.description

        holder.itemView.setOnClickListener { listener.onEventClick(event) }
        holder.itemView.setOnLongClickListener {
            listener.onEventLongClick(event)
            true
        }
    }

    override fun getItemCount() = events.size

    fun submitList(newList: MutableList<Event>) {
        events = newList
        notifyDataSetChanged()
    }
}
