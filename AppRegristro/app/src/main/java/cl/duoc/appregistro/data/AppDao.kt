package cl.duoc.appregistro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM users ORDER BY id") fun users(): Flow<List<User>>
    @Query("SELECT * FROM users WHERE active = 1 ORDER BY name") fun activeUsers(): Flow<List<User>>
    @Query("SELECT * FROM users WHERE lower(email) = lower(:email) AND password = :password AND active = 1 LIMIT 1") suspend fun login(email: String, password: String): User?
    @Query("SELECT COUNT(*) FROM users") suspend fun userCount(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertUsers(users: List<User>)
    @Insert suspend fun addUser(user: User): Long
    @Query("SELECT * FROM organizations ORDER BY name") fun organizations(): Flow<List<Organization>>
    @Insert suspend fun addOrganization(item: Organization): Long
    @Update suspend fun updateOrganization(item: Organization)
    @Query("SELECT * FROM contacts ORDER BY name") fun contacts(): Flow<List<Contact>>
    @Insert suspend fun addContact(item: Contact): Long
    @Update suspend fun updateContact(item: Contact)
    @Query("SELECT * FROM opportunities ORDER BY eventDate") fun allOpportunities(): Flow<List<Opportunity>>
    @Query("SELECT * FROM opportunities WHERE assignedUserId = :userId ORDER BY eventDate") fun myOpportunities(userId: Long): Flow<List<Opportunity>>
    @Query("SELECT * FROM opportunities WHERE id = :id LIMIT 1") suspend fun opportunity(id: Long): Opportunity?
    @Insert suspend fun addOpportunity(item: Opportunity): Long
    @Update suspend fun updateOpportunity(item: Opportunity)
    @Query("SELECT COUNT(*) FROM opportunities") suspend fun opportunityCount(): Int
    @Query("SELECT * FROM followups ORDER BY dateTime DESC") fun followups(): Flow<List<FollowUp>>
    @Query("SELECT * FROM followups WHERE opportunityId = :opportunityId ORDER BY dateTime DESC") fun followupsFor(opportunityId: Long): Flow<List<FollowUp>>
    @Insert suspend fun addFollowUp(item: FollowUp): Long
}
