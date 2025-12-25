package com.example.projectqlbenhan.dao.accountDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectqlbenhan.entity.account.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE username = :user AND passwordHash = :pass LIMIT 1")
    suspend fun     login(user: String, pass: String): Account?

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE username = :username)")
    suspend fun isUsernameExist(username: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: Account): Long

    @Query("UPDATE accounts SET passwordHash = :newPass WHERE accountId = :id")
    suspend fun updatePassword(id: Long, newPass: String)
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAccounts(): Int

    @Query("SELECT * FROM accounts WHERE accountId = :accountId")
    suspend fun getAccountById(accountId: Long): Account

    @Delete
    suspend fun Delete(account: Account)

}