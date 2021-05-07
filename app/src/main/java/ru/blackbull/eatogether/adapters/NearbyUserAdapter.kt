package ru.blackbull.eatogether.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.android.synthetic.main.item_user_preview.view.*
import ru.blackbull.eatogether.R
import ru.blackbull.eatogether.models.firebase.User

/**
 * Адаптер для отображения пользователей рядом (карточка тиндера)
 *
 */
class NearbyUserAdapter : RecyclerView.Adapter<NearbyUserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User , newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User , newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this , differCallback)

    var users: List<User>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user_preview ,
                parent ,
                false
            )
        )
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        val user = users[position]
        holder.itemView.apply {
            ivNearbyUserPhoto.load(user.imageUri)
            tvNearbyUserName.text = "${user.firstName} ${user.lastName}"
            tvNearbyUserDescription.text = user.description
        }
    }
}