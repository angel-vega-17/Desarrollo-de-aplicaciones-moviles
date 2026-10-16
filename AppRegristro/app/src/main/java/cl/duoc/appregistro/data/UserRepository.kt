package cl.duoc.appregistro.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {
    val users: Flow<List<User>> = dao.getAll()

    suspend fun emailExists(email: String): Boolean = dao.countByEmail(email) > 0
    suspend fun add(user: User) = dao.insert(user)
    suspend fun remove(user: User) = dao.delete(user)
}
