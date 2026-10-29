package com.example.exp12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtIdBadge: TextView = itemView.findViewById(R.id.txtUserIdBadge)
        val txtName: TextView = itemView.findViewById(R.id.txtUserName)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUserUsername)
        val txtEmail: TextView = itemView.findViewById(R.id.txtUserEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.txtIdBadge.text = user.id.toString()
        holder.txtName.text = user.name
        holder.txtUsername.text = "@${user.username}"
        holder.txtEmail.text = user.email

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateUsers(newUsers: List<User>) {
        userList = newUsers
        notifyDataSetChanged()
    }
}