package com.example.projectqlbenhan.ui.BacSi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.ui.BacSi.Adapter_BacSi
import com.example.projectqlbenhan.ui.BacSi.screenBacSi_Detail
import com.example.projectqlbenhan.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class screenBacSi_Main : AppCompatActivity() {

    // Khai báo view
    private lateinit var btnBack: View // Dùng View cho tổng quát (có thể là ImageView)
    private lateinit var etSearch: EditText
    private lateinit var rvBacSi: RecyclerView
    private lateinit var btnAdd: TextView // Đã sửa thành TextView theo layout mới

    // Khai báo data
    private var originalList: List<Doctor> = listOf()
    private lateinit var adapter: Adapter_BacSi

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadDoctors()
            etSearch.setText("")
        }
    }

    override fun onResume() {
        loadDoctors()
        return super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_bac_si_main)

        // Xử lý Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        setupRecyclerView()
        setEvent()
        loadDoctors()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        rvBacSi = findViewById(R.id.rvBacSi)

        // Ánh xạ nút thêm (TextView)
        btnAdd = findViewById(R.id.btnAdd)
    }

    private fun setupRecyclerView() {
        adapter = Adapter_BacSi(listOf()) { doctor ->
            val intent = Intent(this, screenBacSi_Detail::class.java)
            intent.putExtra("doctor_id", doctor.doctorId)
            launcher.launch(intent)
        }

        rvBacSi.layoutManager = LinearLayoutManager(this)
        rvBacSi.adapter = adapter
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Sự kiện click nút "+ Thêm"
        btnAdd.setOnClickListener {
            val intent = Intent(this, screenBacSi_Add::class.java)
            launcher.launch(intent)
        }



        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDoctors() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Main)
            originalList = db.doctorDao().getAll()

            withContext(Dispatchers.Main) {
                adapter.updateData(originalList)
            }
        }
    }

    private fun filterList(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(originalList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            val filteredList = originalList.filter { doctor ->
                doctor.fullName.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        doctor.specialization?.lowercase(Locale.getDefault())!!.contains(lowerCaseQuery)
            }
            adapter.updateData(filteredList)
        }
    }
}