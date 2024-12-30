package com.example.firestoreintegration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(var students :ArrayList<Model>, val studentInterface: StudentInterface)
    : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {
    class ViewHolder( view: View) : RecyclerView.ViewHolder(view){
        val name=view.findViewById<TextView>(R.id.tvName)
        val Class=view.findViewById<TextView>(R.id.tvClass)
        val deleteBtn=view.findViewById<Button>(R.id.deleteBtn)
        val updateBtn=view.findViewById<Button>(R.id.updateBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  students.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text=students[position].Name
        holder.Class.text=students[position].Class
        holder.itemView.setOnClickListener {studentInterface.onClick(position,students[position])}
        holder.updateBtn.setOnClickListener {studentInterface.update(position)}
        holder.deleteBtn.setOnClickListener { studentInterface.delete(position) }
    }

}
