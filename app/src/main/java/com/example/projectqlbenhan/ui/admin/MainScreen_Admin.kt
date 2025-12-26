package com.example.projectqlbenhan.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.BacSi.screenBacSi_Main
import com.example.projectqlbenhan.ui.DanhGia.ScreenDanhGia_Main
import com.example.projectqlbenhan.ui.authService.Login
import com.example.projectqlbenhan.utils.SessionManager


class MainScreen_Admin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen_admin)

        findViewById<CardView>(R.id.cardManageDoctors).setOnClickListener {
            val intent = Intent(this, screenBacSi_Main::class.java)
            startActivity(intent)
        }


        findViewById<CardView>(R.id.cardReviews).setOnClickListener {
            val intent = Intent(this, ScreenDanhGia_Main::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardLogout).setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}