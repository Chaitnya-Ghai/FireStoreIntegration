package com.example.firestoreintegration

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreintegration.databinding.ActivityMainBinding
import com.example.firestoreintegration.databinding.CustomDialogBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class MainActivity : AppCompatActivity(), StudentInterface {
    lateinit var binding: ActivityMainBinding
    private var array = arrayListOf<Model>()
    lateinit var recyclerAdapter: StudentAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var customDialogBinding: CustomDialogBinding
    var collectionName = "students"
//    firebase Variables
    val db=Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        customDialogBinding = CustomDialogBinding.inflate(layoutInflater)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerAdapter = StudentAdapter(array, this)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = recyclerAdapter
        binding.fab.setOnClickListener { dialog(-1, -1) }
        db.collection(collectionName).addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (snapshot in snapshots!!.documentChanges) {
                val model = convertObject(snapshot.document)
                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> {
                        model?.let { array.add(it) }
                        Log.e("TAG", "array size ${array.size}:")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        model?.let {
                            val index = getIndex(model)
                            if (index > -1)
                                array.set(index, it)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        model?.let {
                            val index = getIndex(it)
                            if (index > -1)
                                array.removeAt(index)
                        }
                    }
                }
            }
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun getIndex(model: Model): Int {
        var index=-1
        index = array.indexOfFirst { ele->
            ele.Id?.equals(model.Id)==true
        }
        return index
    }

    private fun convertObject(snapshot: QueryDocumentSnapshot): Model? {
        val model :Model?=snapshot.toObject(Model::class.java)
        model?.Id=snapshot.id
        return model
    }


    private fun dialog(upKey:Int,position: Int){
        val dialog = Dialog(this).apply {
            setContentView(customDialogBinding.root)
            setCancelable(true)
            window?.setLayout(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        fun checkValids() {
            if (customDialogBinding.name.text.toString().isBlank()){
                customDialogBinding.name.error="Enter Student Name"
            }
            if (customDialogBinding.rollNo.text.toString().isBlank()){
                customDialogBinding.rollNo.error="Enter Student RollNo"
            }
            if (customDialogBinding.Class.text.toString().isBlank()) {
                customDialogBinding.Class.error = "Enter Student Class"
            }

        }
        if (upKey== -1){
//            add new item
//            use function for validation
            customDialogBinding.sBtn.setOnClickListener {
                checkValids()
                val info=Model("",
                    customDialogBinding.name.text.toString().trim(),
                    customDialogBinding.Class.text.toString().trim(),
                    customDialogBinding.rollNo.text.toString().trim())
                Log.e("see", "collectionName:${collectionName}")
                db.collection(collectionName).add(info).addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(this, "DATA Saved", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            customDialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
        }
        if (upKey == 1){
//            update the item
//            get the old data then validation
            customDialogBinding.name.setText(array[position].Name)
            customDialogBinding.Class.setText(array[position].Class)
            customDialogBinding.rollNo.setText(array[position].rollNO)
            customDialogBinding.sBtn.setText("Update")
            customDialogBinding.sBtn.setOnClickListener {
                checkValids()
                val info=Model("",
                    customDialogBinding.name.text.toString().trim(),customDialogBinding.Class.text.toString().trim(),
                    customDialogBinding.rollNo.text.toString().trim())
                dialog.dismiss()
            }
        }
    }
    override fun delete(position: Int) {
        AlertDialog.Builder(this).apply {
            setTitle("Are you sure")
            setPositiveButton("Delete"){_,_ ->
                db.collection(collectionName).document(array[position].Id?:"").delete()
            }
            setNeutralButton("NO"){_,_ ->}
            setCancelable(true)
            show()
        }
//        array.removeAt(position)
//        recyclerAdapter.notifyDataSetChanged()
    }

    override fun update(position: Int) {
        dialog(1,position)
        recyclerAdapter.notifyDataSetChanged()
    }

    override fun onClick(position: Int, model: Model) {

    }
}