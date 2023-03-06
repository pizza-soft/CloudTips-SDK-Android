package ru.cloudtips.sdk.ui.activities.tips.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.cloudtips.sdk.R

class BubblesAdapter(private val items: List<Int>, private val listener: IBubblesAdapterListener) :
    RecyclerView.Adapter<BubblesAdapter.ViewHolder>() {

    interface IBubblesAdapterListener {
        fun onBubbleClicked(item: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(R.id.text_view)
        fun bind(position: Int) {
            val item = items[position]
            textView.text = itemView.context.getString(R.string.bubble_text, item)
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) listener.onBubbleClicked(items[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_payment_bubble, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = items.size
}