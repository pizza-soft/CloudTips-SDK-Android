package ru.cloudtips.sdk.ui.activities.tips.adapters

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.models.RatingComponent


class ComponentsAdapter(private val items: List<RatingComponent>, private val listener: IComponentsAdapterListener) :
    RecyclerView.Adapter<ComponentsAdapter.ViewHolder>() {

    interface IComponentsAdapterListener {
        fun onItemClicked(item: RatingComponent)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView = itemView.findViewById<TextView>(R.id.text_view)
        private val iconImageView = itemView.findViewById<AppCompatImageView>(R.id.image_view)

        fun bind(position: Int) {
            val item = items[position]
            titleTextView.text = item.title

            Glide.with(iconImageView)
                .load(item.image)
                .error(R.drawable.ic_rating_component_default)
                .into(iconImageView)

            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(items[bindingAdapterPosition])
                }
            }
            itemView.isSelected = item.selected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_rating_component, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = items.size
}