package com.samikhan.draven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textView.text = message.content
        
        // Set background and alignment based on message role
        if (message.role == "user") {
            holder.textView.setBackgroundResource(R.drawable.premium_user_message_bg)
            holder.textView.layoutParams = (holder.textView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 40
                marginEnd = 4
            }
            
            // Animate user messages from right
            val slideInRight = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_right)
            slideInRight.duration = 300
            slideInRight.startOffset = (position * 100).toLong()
            holder.itemView.startAnimation(slideInRight)
        } else {
            holder.textView.setBackgroundResource(R.drawable.premium_ai_message_bg)
            holder.textView.layoutParams = (holder.textView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 4
                marginEnd = 40
            }
            
            // Animate AI messages from left
            val slideInLeft = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_left)
            slideInLeft.duration = 300
            slideInLeft.startOffset = (position * 100).toLong()
            holder.itemView.startAnimation(slideInLeft)
        }
    }

    override fun getItemCount() = messages.size
} 