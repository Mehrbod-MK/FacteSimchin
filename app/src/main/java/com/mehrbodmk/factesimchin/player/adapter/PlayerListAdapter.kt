package com.mehrbodmk.factesimchin.player.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mehrbodmk.factesimchin.R
import com.mehrbodmk.factesimchin.databinding.PlayerListItemBinding
import com.mehrbodmk.factesimchin.models.PlayerPresence
import com.mehrbodmk.factesimchin.utils.Helpers
import timber.log.Timber

class PlayerListAdapter : ListAdapter<PlayerPresence, PlayerListAdapter.PlayerViewHolder>(object : DiffUtil.ItemCallback<PlayerPresence>() {
    override fun areItemsTheSame(
        oldItem: PlayerPresence,
        newItem: PlayerPresence
    ): Boolean = oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: PlayerPresence,
        newItem: PlayerPresence
    ): Boolean = oldItem == newItem
}) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlayerViewHolder(PlayerListItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, position)
    }


    inner class PlayerViewHolder(val binding : PlayerListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player : PlayerPresence, position: Int) {
            binding.player = player
            binding.position = position.toString()
            binding.listItemRemoveButton.setOnClickListener {
                Helpers.playSoundEffect(itemView.context, R.raw.button)
                //mObjects.removeAt(position)
                //notifyDataSetChanged()
            }
            binding.buttonSelectPlayerNameItem.setOnClickListener {
                val status = !player.isPresent
                player.isPresent = status
                Helpers.playSoundEffect(itemView.context, if(status) R.raw.checkbox_on else R.raw.checkbox_off)
            }
        }
    }

}