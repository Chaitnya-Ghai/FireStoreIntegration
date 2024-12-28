package com.example.firestoreintegration

interface StudentInterface {
    fun delete(position: Int)
    fun update(position: Int)
    fun onClick(position: Int, model: Model)
}
