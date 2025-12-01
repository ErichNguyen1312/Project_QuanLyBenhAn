package com.example.projectqlbenhan

import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListBenhNhan : AppCompatActivity() {


     lateinit var btnBack: ImageView
     lateinit var btnAddHeader: TextView
     lateinit var fabAdd: FrameLayout
     lateinit var etSearch: EditText
     lateinit var layoutList: LinearLayout

     lateinit var navHome: LinearLayout
     lateinit var navPatients: LinearLayout
     lateinit var navRecord: LinearLayout
     lateinit var navMore: LinearLayout


    private val dsBenhNhan = mutableListOf(
        BenhNhan("Nguyễn Văn A", 34, "Nam", "BN001"),
        BenhNhan("Trần Thị B", 28, "Nữ", "BN002"),
        BenhNhan("Lê Văn C", 45, "Nam", "BN003"),
        BenhNhan("Phạm Thị D", 52, "Nữ", "BN004"),
        BenhNhan("Hoàng Văn E", 60, "Nam", "BN005")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_benh_nhan)

        setControl()
        setEvent()

        loadDanhSach(dsBenhNhan)
    }

// anh xa
    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)
        btnAddHeader = findViewById(R.id.btnAddHeader)
        fabAdd = findViewById(R.id.fabAdd)
        etSearch = findViewById(R.id.etSearch)

//        layoutList = findViewById(R.id.layoutList)

//        navHome = findViewById(R.id.navHome)
//        navPatients = findViewById(R.id.navPatients)
//        navRecord = findViewById(R.id.navRecord)
//        navMore = findViewById(R.id.navMore)
    }

    //click
    private fun setEvent() {


        btnBack.setOnClickListener {
            finish()
        }


        btnAddHeader.setOnClickListener {
            Toast.makeText(this, "Thêm bệnh nhân", Toast.LENGTH_SHORT).show()
        }

        fabAdd.setOnClickListener {
            Toast.makeText(this, "Thêm bệnh nhân", Toast.LENGTH_SHORT).show()
        }

//        etSearch.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(txt: CharSequence?, start: Int, before: Int, count: Int) {
//                val keyword = txt.toString().trim().lowercase()
//
//                val dsLoc = dsBenhNhan.filter {
//                    it.name.lowercase().contains(keyword) ||
//                            it.recordId.lowercase().contains(keyword)
//                }
//
//                loadDanhSach(dsLoc)
//            }
//        })

        navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }
        navPatients.setOnClickListener {
            Toast.makeText(this, "Đang ở Patients", Toast.LENGTH_SHORT).show()
        }
        navRecord.setOnClickListener {
            Toast.makeText(this, "Record", Toast.LENGTH_SHORT).show()
        }
        navMore.setOnClickListener {
            Toast.makeText(this, "More", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadDanhSach(list: List<BenhNhan>) {
        layoutList.removeAllViews()

        for (bn in list) {
            val viewItem = layoutInflater.inflate(R.layout.item_patient, null)

            val tvName = viewItem.findViewById<TextView>(R.id.tvName)
            val tvInfo = viewItem.findViewById<TextView>(R.id.tvInfo)

            tvName.text = bn.name
            tvInfo.text = "${bn.age} tuổi • ${bn.gender} • Mã HS: ${bn.recordId}"

            // Sự kiện click từng item
            viewItem.setOnClickListener {
                Toast.makeText(this, "Xem ${bn.name}", Toast.LENGTH_SHORT).show()
            }

            layoutList.addView(viewItem)
        }
    }
}