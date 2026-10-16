package cl.duoc.appregistro.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.appregistro.data.AppDatabase
import cl.duoc.appregistro.data.User
import cl.duoc.appregistro.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val name: String = "",
    val email: String = "",
    val photoUri: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val users: List<User> = emptyList(),
    val saved: Boolean = false
)

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(
        AppDatabase.getInstance(application).userDao()
    )

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.users.collect { list ->
                _uiState.update { it.copy(users = list) }
            }
        }
    }

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null) }

    fun onPhotoChange(uri: String) =
        _uiState.update { it.copy(photoUri = uri) }

    fun saveUser() {
        val s = _uiState.value
        val nameError = when {
            s.name.isBlank() -> "El nombre es obligatorio"
            s.name.trim().length < 3 -> "Mínimo 3 caracteres"
            else -> null
        }
        val emailError = when {
            s.email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() ->
                "Formato de correo inválido"
            else -> null
        }
        if (nameError != null || emailError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError) }
            return
        }
        viewModelScope.launch {
            if (repository.emailExists(s.email.trim())) {
                _uiState.update { it.copy(emailError = "Este correo ya está registrado") }
                return@launch
            }
            repository.add(
                User(name = s.name.trim(), email = s.email.trim(), photoUri = s.photoUri)
            )
            _uiState.update {
                it.copy(name = "", email = "", photoUri = null, saved = true)
            }
        }
    }

    fun onSavedHandled() = _uiState.update { it.copy(saved = false) }

    fun deleteUser(user: User) {
        viewModelScope.launch { repository.remove(user) }
    }
}
