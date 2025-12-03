package com.example.projectqlbenhan


    data class Patient(
        var name: String,
        var age: Int,
        var gender: String,
        var recordId: String,
        var address: String = "",
        var phone: String = ""
    )
