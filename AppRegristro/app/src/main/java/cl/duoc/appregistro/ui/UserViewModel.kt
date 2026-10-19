package cl.duoc.appregistro.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.appregistro.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppUiState(
    val currentUser: User? = null,
    val opportunitySaved: Boolean = false,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val message: String? = null,
    val loading: Boolean = false,
    val opportunities: List<Opportunity> = emptyList(),
    val organizations: List<Organization> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val users: List<User> = emptyList(),
    val followups: List<FollowUp> = emptyList(),
    val search: String = "",
    val statusFilter: String = "Todos",
    val selectedOpportunity: Opportunity? = null,
    val formName: String = "",
    val formType: String = "Evento corporativo",
    val formDescription: String = "",
    val formSource: String = "Sitio web ficticio",
    val formDetectedDate: String = today(),
    val formEventDate: String = today(),
    val formEndDate: String = today(),
    val formTime: String = "10:00",
    val formLocation: String = "",
    val formCommune: String = "Puente Alto",
    val formRegion: String = "Metropolitana",
    val formOrganizationId: String = "1",
    val formAssignedUserId: String = "1",
    val formAudience: String = "100",
    val formTargetAudience: String = "Trabajadores y familias",
    val formPriority: String = "Media",
    val formCost: String = "0",
    val formStatus: String = "Evento identificado",
    val formConditions: String = "",
    val formNotes: String = "",
    val formPhotoUri: String? = null,
    val formError: String? = null,
    val orgName: String = "",
    val orgType: String = "Empresa",
    val orgIndustry: String = "Servicios",
    val orgCommune: String = "Puente Alto",
    val orgRegion: String = "Metropolitana",
    val orgWebsite: String = "",
    val orgNotes: String = "",
    val contactName: String = "",
    val contactOrgId: String = "1",
    val contactPosition: String = "",
    val contactArea: String = "Marketing",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val contactNotes: String = "",
    val followOpportunityId: String = "",
    val followType: String = "Seguimiento",
    val followResult: String = "",
    val followNextAction: String = "",
    val followNextDate: String = today(),
    val followNotes: String = ""
) {
}
private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).dao()
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    val opportunities = dao.allOpportunities().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val organizations = dao.organizations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val contacts = dao.contacts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = dao.activeUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val followups = dao.followups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (dao.userCount() == 0) dao.insertUsers(listOf(
                User(name="Ejecutivo Uno", email="ejecutivo1@demo.cl", password="demo123", role="EJECUTIVO"),
                User(name="Ejecutivo Dos", email="ejecutivo2@demo.cl", password="demo123", role="EJECUTIVO"),
                User(name="Ejecutivo Tres", email="ejecutivo3@demo.cl", password="demo123", role="EJECUTIVO"),
                User(name="Supervisor Comercial", email="supervisor@demo.cl", password="demo123", role="SUPERVISOR")
            ))
            if (dao.organizationCount() == 0) {
                dao.addOrganization(Organization(name="Empresa Demo Norte", type="Empresa", industry="Servicios", commune="Puente Alto", region="Metropolitana", website="https://demo.invalid"))
                dao.addOrganization(Organization(name="Municipalidad Ficticia", type="Municipalidad", industry="Sector público", commune="La Florida", region="Metropolitana"))
            }
            launch { dao.users().collect { list -> _uiState.update { it.copy(users=list) } } }
            launch { opportunities.collect { list -> _uiState.update { it.copy(opportunities=list) } } }
            launch { organizations.collect { list -> _uiState.update { it.copy(organizations=list) } } }
            launch { contacts.collect { list -> _uiState.update { it.copy(contacts=list) } } }
            launch { followups.collect { list -> _uiState.update { it.copy(followups=list) } } }
        }
    }

    fun update(transform: AppUiState.() -> AppUiState) = _uiState.update { it.transform() }
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = dao.login(email.trim(), password)
            if (user == null) _uiState.update { it.copy(message="Correo o contraseña incorrectos.") }
            else _uiState.update { it.copy(currentUser=user, message=null, loginPassword="") }
        }
    }
    fun logout() = _uiState.update { it.copy(currentUser=null, selectedOpportunity=null, message=null) }
    fun clearMessage() = _uiState.update { it.copy(message=null) }
    fun newOpportunity() = _uiState.update { s -> s.copy(selectedOpportunity=null, formName="", formDescription="", formSource="Sitio web ficticio", formDetectedDate=today(), formEventDate=today(), formEndDate=today(), formTime="10:00", formLocation="", formCommune="Puente Alto", formRegion="Metropolitana", formOrganizationId=s.organizations.firstOrNull()?.id?.toString() ?: "1", formAssignedUserId=s.currentUser?.id?.toString() ?: "1", formAudience="100", formTargetAudience="Trabajadores y familias", formPriority="Media", formCost="0", formStatus="Evento identificado", formConditions="", formNotes="", formPhotoUri=null, formError=null) }
    fun editOpportunity(item: Opportunity) = _uiState.update { it.copy(selectedOpportunity=item, formName=item.name, formType=item.type, formDescription=item.description, formSource=item.source, formDetectedDate=item.detectedDate, formEventDate=item.eventDate, formEndDate=item.endDate, formTime=item.time, formLocation=item.location, formCommune=item.commune, formRegion=item.region, formOrganizationId=item.organizationId.toString(), formAssignedUserId=item.assignedUserId.toString(), formAudience=item.expectedAudience.toString(), formTargetAudience=item.targetAudience, formPriority=item.priority, formCost=item.estimatedCost.toString(), formStatus=item.status, formConditions=item.conditions, formNotes=item.notes, formPhotoUri=item.photoUri, formError=null) }
    fun saveOpportunity() {
        val s = _uiState.value
        val cost = s.formCost.toDoubleOrNull()
        val audience = s.formAudience.toIntOrNull()
        val orgId = s.formOrganizationId.toLongOrNull()
        val assignedId = s.formAssignedUserId.toLongOrNull()
        val error = when {
            s.formName.isBlank() -> "El nombre de la oportunidad es obligatorio."
            s.formDescription.isBlank() -> "La descripción es obligatoria."
            !validDate(s.formEventDate) || !validDate(s.formEndDate) || !validDate(s.formDetectedDate) -> "Usa el formato de fecha AAAA-MM-DD en todas las fechas."
            s.formEndDate < s.formEventDate -> "La fecha de término no puede ser anterior al inicio."
            !Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$").matches(s.formTime) -> "El horario debe usar formato HH:MM."
            s.formLocation.isBlank() -> "La ubicación es obligatoria."
            orgId == null || s.organizations.none { it.id == orgId } -> "Selecciona una organización válida usando su ID."
            assignedId == null || s.users.none { it.id == assignedId } -> "Selecciona un responsable válido usando su ID."
            audience == null || audience < 0 -> "El público esperado debe ser un número igual o mayor a cero."
            cost == null || cost < 0 -> "El costo debe ser un número igual o mayor a cero."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(formError=error) }; return }
        viewModelScope.launch {
            val user = s.currentUser ?: return@launch
            val isSupervisor = user.role == "SUPERVISOR"
            if (!isSupervisor && s.selectedOpportunity != null && s.selectedOpportunity.assignedUserId != user.id) {
                _uiState.update { it.copy(formError="No tienes permiso para editar esta oportunidad.") }; return@launch
            }
            if (!isSupervisor && s.formStatus in listOf("Participación confirmada", "Oportunidad descartada")) {
                _uiState.update { it.copy(formError="Solo el supervisor puede confirmar o descartar definitivamente.") }; return@launch
            }
            val item = Opportunity(id=s.selectedOpportunity?.id ?: 0, name=s.formName.trim(), type=s.formType, description=s.formDescription.trim(), source=s.formSource.trim(), detectedDate=s.formDetectedDate, eventDate=s.formEventDate, endDate=s.formEndDate, time=s.formTime, location=s.formLocation.trim(), commune=s.formCommune.trim(), region=s.formRegion.trim(), organizationId=orgId!!, assignedUserId=if (isSupervisor) assignedId!! else (s.selectedOpportunity?.assignedUserId ?: user.id), expectedAudience=audience!!, targetAudience=s.formTargetAudience.trim(), priority=s.formPriority, estimatedCost=cost!!, status=if (isSupervisor) s.formStatus else if (s.formStatus in listOf("Participación confirmada", "Oportunidad descartada")) s.selectedOpportunity?.status ?: "Evento identificado" else s.formStatus, conditions=s.formConditions.trim(), notes=s.formNotes.trim(), photoUri=s.formPhotoUri, updatedAt=today())
            if (s.selectedOpportunity == null) dao.addOpportunity(item) else dao.updateOpportunity(item)
            _uiState.update { it.copy(message="Oportunidad guardada correctamente.", formError=null, selectedOpportunity=null, opportunitySaved=true) }
        }
    }
    fun setStatus(item: Opportunity, status: String) {
        val user = _uiState.value.currentUser ?: return
        if (status in listOf("Participación confirmada", "Oportunidad descartada") && user.role != "SUPERVISOR") {
            _uiState.update { it.copy(message="Solo el supervisor puede confirmar o descartar definitivamente.") }; return
        }
        if (user.role != "SUPERVISOR" && item.assignedUserId != user.id) {
            _uiState.update { it.copy(message="No tienes permiso para modificar esta oportunidad.") }; return
        }
        viewModelScope.launch { dao.updateOpportunity(item.copy(status=status, updatedAt=today())); _uiState.update { it.copy(message="Estado actualizado.") } }
    }
    fun addOrganization() {
        val s = _uiState.value
        val error = when { s.orgName.isBlank() -> "El nombre de la organización es obligatorio."; s.orgCommune.isBlank() || s.orgRegion.isBlank() -> "Comuna y región son obligatorias."; else -> null }
        if (error != null) { _uiState.update { it.copy(message=error) }; return }
        viewModelScope.launch { dao.addOrganization(Organization(name=s.orgName.trim(), type=s.orgType, industry=s.orgIndustry, commune=s.orgCommune.trim(), region=s.orgRegion.trim(), website=s.orgWebsite.trim(), notes=s.orgNotes.trim())); _uiState.update { it.copy(orgName="", orgWebsite="", orgNotes="", message="Organización guardada.") } }
    }
    fun addContact() {
        val s = _uiState.value
        val org = s.contactOrgId.toLongOrNull()
        val error = when { s.contactName.isBlank() -> "El nombre del contacto es obligatorio."; org == null || s.organizations.none { it.id == org } -> "Ingresa un ID de organización válido."; s.contactEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(s.contactEmail).matches() -> "El correo no tiene un formato válido."; s.contactPhone.isNotBlank() && s.contactPhone.length < 7 -> "El teléfono debe tener al menos 7 caracteres."; else -> null }
        if (error != null) { _uiState.update { it.copy(message=error) }; return }
        viewModelScope.launch { dao.addContact(Contact(organizationId=org!!, name=s.contactName.trim(), position=s.contactPosition.trim(), area=s.contactArea, email=s.contactEmail.trim(), phone=s.contactPhone.trim(), notes=s.contactNotes.trim())); _uiState.update { it.copy(contactName="", contactPosition="", contactEmail="", contactPhone="", contactNotes="", message="Contacto guardado.") } }
    }
    fun addFollowUp() {
        val s = _uiState.value
        val oppId = s.followOpportunityId.toLongOrNull()
        val user = s.currentUser
        val error = when { oppId == null || s.opportunities.none { it.id == oppId } -> "Ingresa un ID de oportunidad válido."; s.followResult.isBlank() -> "El resultado de la gestión es obligatorio."; !validDate(s.followNextDate) -> "La fecha de seguimiento debe usar formato AAAA-MM-DD."; user == null -> "Inicia sesión para registrar una gestión."; s.opportunities.firstOrNull { it.id == oppId }?.assignedUserId != user.id && user.role != "SUPERVISOR" -> "No tienes permiso para registrar seguimiento en esta oportunidad."; else -> null }
        if (error != null) { _uiState.update { it.copy(message=error) }; return }
        viewModelScope.launch { dao.addFollowUp(FollowUp(opportunityId=oppId!!, userId=user!!.id, actionType=s.followType, dateTime=today()+" 09:00", result=s.followResult.trim(), nextAction=s.followNextAction.trim(), nextDate=s.followNextDate, notes=s.followNotes.trim())); _uiState.update { it.copy(followResult="", followNextAction="", followNotes="", message="Seguimiento registrado.") } }
    }
    private fun validDate(value: String): Boolean = runCatching { java.time.LocalDate.parse(value) }.isSuccess

    fun assign(item: Opportunity, userId: Long) {
        if (_uiState.value.currentUser?.role != "SUPERVISOR") return
        viewModelScope.launch { dao.updateOpportunity(item.copy(assignedUserId=userId, updatedAt=today())); _uiState.update { it.copy(message="Responsable reasignado.") } }
    }
}
