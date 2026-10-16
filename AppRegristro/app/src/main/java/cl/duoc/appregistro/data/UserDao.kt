package cl.duoc.appregistro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    @Insert
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)
}
