package cl.duoc.appregistro.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val area: String = "Comercial",
    val active: Boolean = true
)

@Entity(tableName = "organizations")
data class Organization(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val industry: String,
    val commune: String,
    val region: String,
    val website: String = "",
    val notes: String = ""
)

@Entity(tableName = "contacts", foreignKeys = [ForeignKey(entity = Organization::class, parentColumns = ["id"], childColumns = ["organizationId"], onDelete = ForeignKey.CASCADE)], indices = [Index("organizationId")])
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val name: String,
    val position: String,
    val area: String,
    val email: String,
    val phone: String,
    val preferredMethod: String = "Correo",
    val notes: String = ""
)

@Entity(tableName = "opportunities", foreignKeys = [
    ForeignKey(entity = Organization::class, parentColumns = ["id"], childColumns = ["organizationId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["assignedUserId"], onDelete = ForeignKey.RESTRICT)
], indices = [Index("organizationId"), Index("assignedUserId"), Index("status"), Index("priority")])
data class Opportunity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val description: String,
    val source: String,
    val detectedDate: String,
    val eventDate: String,
    val endDate: String,
    val time: String,
    val location: String,
    val commune: String,
    val region: String,
    val organizationId: Long,
    val assignedUserId: Long,
    val expectedAudience: Int,
    val targetAudience: String,
    val priority: String,
    val estimatedCost: Double,
    val status: String = "Evento identificado",
    val conditions: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val updatedAt: String = ""
)

@Entity(tableName = "followups", foreignKeys = [
    ForeignKey(entity = Opportunity::class, parentColumns = ["id"], childColumns = ["opportunityId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.RESTRICT)
], indices = [Index("opportunityId"), Index("userId")])
data class FollowUp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opportunityId: Long,
    val userId: Long,
    val actionType: String,
    val dateTime: String,
    val result: String,
    val nextAction: String,
    val nextDate: String,
    val notes: String
)
