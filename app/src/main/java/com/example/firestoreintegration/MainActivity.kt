package com.example.firestoreintegration

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
        binding.fab.setOnClickListener { dialog() }
        array.clear()
        db.collection(collectionName)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (snapshot in snapshots!!.documentChanges) {
                    val userModel = convertObject(snapshot.document)

                    when (snapshot.type) {
                        DocumentChange.Type.ADDED -> {
                            userModel?.let { array.add(it) }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            userModel?.let {
                                var index = getIndex(userModel)
                                if (index > -1) {
                                    array.set(index, it)
                                }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            userModel?.let {
                                var index = getIndex(userModel)
                                if (index > -1) {
                                    array.removeAt(index)
                                }
                            }
                        }
                    }
                    recyclerAdapter.notifyDataSetChanged()
                }
            }
    }

    fun getIndex(userModel: Model) : Int{
        var index = -1
        index = array.indexOfFirst { element ->
            element.id?.equals(userModel.id) == true
        }
        return index
    }

    fun convertObject(snapshot: QueryDocumentSnapshot) : Model?{
        val userModel: Model = snapshot.toObject(Model::class.java)
        userModel.id = snapshot.id ?: ""
        return userModel
    }


    private fun dialog(position: Int = -1){
        val dialog = Dialog(this).apply {
            setContentView(customDialogBinding.root)
            setCancelable(true)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
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
        if (position== -1){
//            add new item
//            use function for validation
            customDialogBinding.sBtn.setOnClickListener {
                checkValids()
                val info=Model("",
                    customDialogBinding.name.text.toString().trim(),
                    customDialogBinding.Class.text.toString().trim(),
                    customDialogBinding.rollNo.text.toString().trim())
                Toast.makeText(this, "collectionName:${collectionName}", Toast.LENGTH_SHORT).show()
                db.collection(collectionName).add(info).addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(this, "Saved id= ${array[position].id}", Toast.LENGTH_SHORT).show()
                    }
                    else
                        Toast.makeText(this, "not saved", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                recyclerAdapter.notifyDataSetChanged()
            }
        }
        if (position > -1) {
            // Populate dialog fields with old data
            customDialogBinding.name.setText(array[position].Name)
            customDialogBinding.Class.setText(array[position].Class)
            customDialogBinding.rollNo.setText(array[position].rollNO)
            customDialogBinding.sBtn.setText("Update")

            customDialogBinding.sBtn.setOnClickListener {
                checkValids() // Ensure this method validates input fields
                Toast.makeText(this, "get ${array[position].id}", Toast.LENGTH_SHORT).show()
                val docId = array[position].id ?: throw Exception("Document ID cannot be null")
                val model =Model(array[position].id, customDialogBinding.name.text.toString().trim(),customDialogBinding.Class.text.toString().trim(),customDialogBinding.rollNo.text.toString().trim())
                Toast.makeText(this, "model get value", Toast.LENGTH_SHORT).show()
                try {
                    db.collection(collectionName).document(docId)
                        .set(model).addOnSuccessListener {
                            Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
                            dialog.dismiss() // Close the dialog only on success
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("FailureListener", "Error updating document", e)
                        }
                } catch (e: Exception) {
                    Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("Exception", "Error: ${e.message}", e)
                }
            }
        }
        customDialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
    }
    override fun delete(position: Int) {
        AlertDialog.Builder(this).apply {
            setTitle("Are you sure")
            setPositiveButton("Delete"){_,_ ->
                db.collection(collectionName).document(array[position].id?:"").delete()
            }
            setNeutralButton("NO"){_,_ ->}
            setCancelable(true)
            show()
        }
    }

    override fun update(position: Int) {
        Toast.makeText(this, "Clicked: ${position}", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "${array[position].id}", Toast.LENGTH_SHORT).show()

        dialog(position)
    }

    override fun onClick(position: Int, model: Model) {
        Toast.makeText(this, "chl rha hai ${position}", Toast.LENGTH_SHORT).show()
    }
}