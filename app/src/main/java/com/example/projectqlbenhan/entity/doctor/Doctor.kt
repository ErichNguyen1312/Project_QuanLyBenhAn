package com.example.projectqlbenhan.entity.doctor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.account.Account

@Entity(
    tableName = "doctors",
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = ["accountId"],
        childColumns = ["account_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["account_id"], unique = true)]
)
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val doctorId: Long = 0,
    @ColumnInfo(name = "account_id")
    val accountId: Long,
    val fullName: String,
    val specialization: String?, // Chuyên khoa
    val description: String?
)