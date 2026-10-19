package cl.duoc.appregistro.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.appregistro.data.*
import coil.compose.AsyncImage
import java.io.File

private object Routes {
    const val LOGIN = "login"; const val HOME = "home"; const val OPPS = "opportunities"
    const val FORM = "opportunity_form"; const val DETAIL = "detail/{id}"; const val ORGS = "organizations"
    const val CONTACTS = "contacts"; const val FOLLOWUPS = "followups"; const val SUPERVISOR = "supervisor"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val vm: UserViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(state, vm) { nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } } }
        composable(Routes.HOME) { if (state.currentUser == null) LaunchedEffect(Unit) { nav.navigate(Routes.LOGIN) { popUpTo(0) } } else Dashboard(state, vm, nav) }
        composable(Routes.OPPS) { OpportunityList(state, vm, nav) }
        composable(Routes.FORM) { OpportunityForm(state, vm, nav) }
        composable(Routes.DETAIL, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val item = state.opportunities.firstOrNull { it.id == id }
            if (item != null && (state.currentUser?.role == "SUPERVISOR" || item.assignedUserId == state.currentUser?.id)) OpportunityDetail(item, state, vm, nav)
            else if (item != null) LaunchedEffect(id) { vm.update { copy(message="No tienes permiso para consultar esta oportunidad.") }; nav.popBackStack() }
        }
        composable(Routes.ORGS) { OrganizationsScreen(state, vm, nav) }
        composable(Routes.CONTACTS) { ContactsScreen(state, vm, nav) }
        composable(Routes.FOLLOWUPS) { FollowupsScreen(state, vm, nav) }
        composable(Routes.SUPERVISOR) { SupervisorScreen(state, vm, nav) }
    }
    state.message?.let { msg -> AlertDialog(onDismissRequest = vm::clearMessage, title = { Text("Aviso") }, text = { Text(msg) }, confirmButton = { TextButton(onClick = vm::clearMessage) { Text("Aceptar") } }) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun LoginScreen(state: AppUiState, vm: UserViewModel, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("ejecutivo1@demo.cl") }
    var password by remember { mutableStateOf("demo123") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.currentUser) { if (state.currentUser != null) onLogin() }
    Scaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Gestión Comercial", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Oportunidades y activaciones corporativas", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(email, { email = it; error = null }, label = { Text("Correo de prueba") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it; error = null }, label = { Text("Contraseña") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { vm.login(email, password) }, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
            Spacer(Modifier.height(20.dp))
            Text("Cuentas ficticias (contraseña: demo123)", style = MaterialTheme.typography.titleSmall)
            Text("ejecutivo1@demo.cl · ejecutivo2@demo.cl · ejecutivo3@demo.cl")
            Text("supervisor@demo.cl")
            Text("La cuenta se valida localmente en Room.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dashboard(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    val user = state.currentUser ?: return
    val isSupervisor = user.role == "SUPERVISOR"
    val count = state.opportunities.count { isSupervisor || it.assignedUserId == user.id }
    val upcoming = state.opportunities.count { it.eventDate >= java.time.LocalDate.now().toString() && (isSupervisor || it.assignedUserId == user.id) }
    val pending = state.followups.count { it.nextDate.isNotBlank() && it.nextDate <= java.time.LocalDate.now().toString() }
    Page("Panel principal") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Hola, ${user.name}", style = MaterialTheme.typography.headlineSmall); Text(if (isSupervisor) "Supervisor comercial" else "Ejecutivo comercial") }
            IconButton(onClick = { vm.logout(); nav.navigate(Routes.LOGIN) { popUpTo(0) } }) { Icon(Icons.Default.ExitToApp, "Cerrar sesión") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Oportunidades", count.toString(), Modifier.weight(1f)); MetricCard("Eventos próximos", upcoming.toString(), Modifier.weight(1f)); MetricCard("Seguimientos", pending.toString(), Modifier.weight(1f))
        }
        Text("Módulos", style = MaterialTheme.typography.titleLarge)
        NavCard("Oportunidades", "Buscar, filtrar, revisar y registrar eventos") { nav.navigate(Routes.OPPS) }
        NavCard("Organizaciones", "Consultar y registrar empresas e instituciones ficticias") { nav.navigate(Routes.ORGS) }
        NavCard("Contactos corporativos", "Registrar responsables y medios de contacto ficticios") { nav.navigate(Routes.CONTACTS) }
        NavCard("Seguimientos", "Consultar el historial y registrar nuevas gestiones") { nav.navigate(Routes.FOLLOWUPS) }
        if (isSupervisor) NavCard("Panel de supervisor", "Vista global, asignación y estados finales") { nav.navigate(Routes.SUPERVISOR) }
        Text("Los datos son ficticios y se conservan localmente en el dispositivo.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp)) { Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelMedium) } }
}

@Composable
private fun NavCard(title: String, subtitle: String, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (expanded) 0.99f else 1f, label = "cardScale")
    Card(onClick = { expanded = !expanded; onClick() }, modifier = Modifier.fillMaxWidth().scale(scale).animateContentSize(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); AnimatedVisibility(visible = true) { Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpportunityList(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    val user = state.currentUser ?: return
    val isSupervisor = user.role == "SUPERVISOR"
    val visible = state.opportunities.filter { (isSupervisor || it.assignedUserId == user.id) && it.name.contains(state.search, ignoreCase = true) && (state.statusFilter == "Todos" || it.status == state.statusFilter) }
    Page("Oportunidades", onBack = { nav.popBackStack() }) {
        OutlinedTextField(state.search, { vm.update { copy(search = it) } }, modifier = Modifier.fillMaxWidth(), label = { Text("Buscar por nombre") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(state.statusFilter, {}, readOnly = true, label = { Text("Filtrar por estado") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { (listOf("Todos") + listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada", "Activación realizada")).forEach { status -> DropdownMenuItem(text = { Text(status) }, onClick = { vm.update { copy(statusFilter=status) }; expanded = false }) } }
        }
        Button(onClick = { vm.newOpportunity(); nav.navigate(Routes.FORM) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Nueva oportunidad") }
        if (visible.isEmpty()) Text("No hay oportunidades para los filtros seleccionados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        visible.forEach { item ->
            val org = state.organizations.firstOrNull { it.id == item.organizationId }?.name ?: "Organización ${item.organizationId}"
            Card(onClick = { nav.navigate("detail/${item.id}") }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$org · ${item.type}")
                    Text("${item.eventDate} ${item.time} · ${item.commune}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AssistChip(onClick = {}, label = { Text(item.status) }); AssistChip(onClick = {}, label = { Text("Prioridad ${item.priority}") }) }
                }
            }
        }
    }
}

@Composable
private fun OpportunityForm(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    LaunchedEffect(state.opportunitySaved) { if (state.opportunitySaved) { vm.update { copy(opportunitySaved=false) }; vm.clearMessage(); nav.popBackStack() } }
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) pendingUri?.let { vm.update { copy(formPhotoUri=it.toString()) } } }
    fun takePhoto() {
        val dir = File(context.filesDir, "opportunity_images").apply { mkdirs() }
        val file = File.createTempFile("event_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingUri = uri; camera.launch(uri)
    }
    Page(if (state.selectedOpportunity == null) "Nueva oportunidad" else "Editar oportunidad", onBack = { nav.popBackStack() }) {
        Field("Nombre de la oportunidad *", state.formName) { vm.update { copy(formName=it, formError=null) } }
        ChoiceField("Tipo de evento", state.formType, listOf("Evento corporativo", "Feria empresarial", "Actividad municipal", "Jornada de beneficios", "Feria institucional", "Activación comercial", "Otro")) { vm.update { copy(formType=it) } }
        Field("Descripción *", state.formDescription, minLines = 2) { vm.update { copy(formDescription=it) } }
        Field("Fuente de información", state.formSource) { vm.update { copy(formSource=it) } }
        Field("Fecha de detección (AAAA-MM-DD) *", state.formDetectedDate) { vm.update { copy(formDetectedDate=it) } }
        Field("Fecha de inicio (AAAA-MM-DD) *", state.formEventDate) { vm.update { copy(formEventDate=it) } }
        Field("Fecha de término (AAAA-MM-DD) *", state.formEndDate) { vm.update { copy(formEndDate=it) } }
        Field("Horario (HH:MM)", state.formTime) { vm.update { copy(formTime=it) } }
        Field("Ubicación o dirección general *", state.formLocation) { vm.update { copy(formLocation=it) } }
        Field("Comuna", state.formCommune) { vm.update { copy(formCommune=it) } }
        Field("Región", state.formRegion) { vm.update { copy(formRegion=it) } }
        ChoiceField("Organización", state.organizations.firstOrNull { it.id.toString() == state.formOrganizationId }?.let { "${it.id} · ${it.name}" } ?: state.formOrganizationId, state.organizations.map { "${it.id} · ${it.name}" }) { selected -> vm.update { copy(formOrganizationId=selected.substringBefore(" · ")) } }
        if (state.currentUser?.role == "SUPERVISOR") ChoiceField("Responsable", state.users.firstOrNull { it.id.toString() == state.formAssignedUserId }?.let { "${it.id} · ${it.name}" } ?: state.formAssignedUserId, state.users.map { "${it.id} · ${it.name}" }) { selected -> vm.update { copy(formAssignedUserId=selected.substringBefore(" · ")) } }
        Field("Público esperado (número)", state.formAudience) { vm.update { copy(formAudience=it) } }
        Field("Público objetivo", state.formTargetAudience) { vm.update { copy(formTargetAudience=it) } }
        ChoiceField("Prioridad", state.formPriority, listOf("Baja", "Media", "Alta", "Urgente")) { vm.update { copy(formPriority=it) } }
        Field("Costo referencial (no negativo)", state.formCost) { vm.update { copy(formCost=it) } }
        if (state.currentUser?.role == "SUPERVISOR") ChoiceField("Estado", state.formStatus, listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada", "Activación realizada", "Activación cancelada")) { vm.update { copy(formStatus=it) } }
        Field("Condiciones de participación", state.formConditions, minLines = 2) { vm.update { copy(formConditions=it) } }
        Field("Observaciones", state.formNotes, minLines = 2) { vm.update { copy(formNotes=it) } }
        OutlinedButton(onClick = ::takePhoto, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(if (state.formPhotoUri == null) "Capturar fotografía del evento" else "Cambiar fotografía") }
        state.formPhotoUri?.let { uri -> AsyncImage(model = uri, contentDescription = "Vista previa de evidencia", modifier = Modifier.fillMaxWidth().height(180.dp)) }
        state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { vm.saveOpportunity() }, modifier = Modifier.fillMaxWidth()) { Text("Guardar oportunidad") }
    }
}

@Composable
private fun OpportunityDetail(item: Opportunity, state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    val context = LocalContext.current
    val org = state.organizations.firstOrNull { it.id == item.organizationId }
    val assigned = state.users.firstOrNull { it.id == item.assignedUserId }
    Page("Detalle de oportunidad", onBack = { nav.popBackStack() }) {
        Text(item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Estado: ${item.status}"); Text("Prioridad: ${item.priority}"); Text("Tipo: ${item.type}")
        Text("Descripción: ${item.description}"); Text("Organización: ${org?.name ?: item.organizationId}")
        Text("Responsable: ${assigned?.name ?: item.assignedUserId}")
        Text("Evento: ${item.eventDate} al ${item.endDate}, ${item.time}")
        Text("Ubicación: ${item.location}, ${item.commune}, ${item.region}")
        Text("Público esperado: ${item.expectedAudience} · Objetivo: ${item.targetAudience}")
        Text("Costo estimado: ${item.estimatedCost}"); Text("Fuente: ${item.source}")
        if (item.conditions.isNotBlank()) Text("Condiciones: ${item.conditions}")
        if (item.notes.isNotBlank()) Text("Observaciones: ${item.notes}")
        item.photoUri?.let { AsyncImage(model = it, contentDescription = "Fotografía del evento", modifier = Modifier.fillMaxWidth().height(190.dp)) }
        val canEdit = state.currentUser?.role == "SUPERVISOR" || item.assignedUserId == state.currentUser?.id
        if (canEdit) Button(onClick = { vm.editOpportunity(item); nav.navigate(Routes.FORM) }, modifier = Modifier.fillMaxWidth()) { Text("Editar oportunidad") }
        if (state.currentUser?.role == "SUPERVISOR") {
            ChoiceField("Estado", item.status, listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada", "Activación realizada", "Activación cancelada")) { vm.setStatus(item, it) }
        } else {
            ChoiceField("Actualizar estado", item.status, listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Requiere seguimiento")) { vm.setStatus(item, it) }
            Text("La confirmación o descarte definitivo está reservada al supervisor.", style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val startMillis = runCatching { parser.parse("${item.eventDate} ${item.time}")?.time }.getOrNull()
            val endMillis = runCatching { parser.parse("${item.endDate} ${item.time}")?.time }.getOrNull()
            val intent = Intent(Intent.ACTION_INSERT).setData(android.provider.CalendarContract.Events.CONTENT_URI)
                .putExtra(android.provider.CalendarContract.Events.TITLE, item.name)
                .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, item.description)
                .putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, "${item.location}, ${item.commune}")
                .putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis ?: 0L)
                .putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endMillis ?: 0L)
            runCatching { context.startActivity(intent) }.onFailure { vm.update { copy(message="No se encontró una aplicación de calendario disponible.") } }
        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text("Programar en calendario") }
        Text("Historial de seguimientos", style = MaterialTheme.typography.titleLarge)
        state.followups.filter { it.opportunityId == item.id }.forEach { Text("${it.dateTime} · ${it.actionType}: ${it.result}") }
        Button(onClick = { vm.update { copy(followOpportunityId=item.id.toString()) }; nav.navigate(Routes.FOLLOWUPS) }, modifier = Modifier.fillMaxWidth()) { Text("Registrar seguimiento") }
    }
}

@Composable
private fun OrganizationsScreen(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    Page("Organizaciones", onBack = { nav.popBackStack() }) {
        Text("Registro de organizaciones ficticias", style = MaterialTheme.typography.titleMedium)
        Field("Nombre de organización *", state.orgName) { vm.update { copy(orgName=it) } }
        ChoiceField("Tipo", state.orgType, listOf("Empresa", "Municipalidad", "Institución pública", "Institución privada", "Organizador de eventos", "Centro comercial", "Asociación", "Otra entidad")) { vm.update { copy(orgType=it) } }
        Field("Rubro", state.orgIndustry) { vm.update { copy(orgIndustry=it) } }
        Field("Comuna *", state.orgCommune) { vm.update { copy(orgCommune=it) } }
        Field("Región *", state.orgRegion) { vm.update { copy(orgRegion=it) } }
        Field("Sitio web ficticio", state.orgWebsite) { vm.update { copy(orgWebsite=it) } }
        Field("Observaciones", state.orgNotes) { vm.update { copy(orgNotes=it) } }
        Button(onClick = vm::addOrganization, modifier = Modifier.fillMaxWidth()) { Text("Guardar organización") }
        Text("Organizaciones registradas", style = MaterialTheme.typography.titleLarge)
        state.organizations.forEach { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("ID ${it.id} · ${it.name}", fontWeight = FontWeight.Bold); Text("${it.type} · ${it.industry}"); Text("${it.commune}, ${it.region}") } } }
    }
}

@Composable
private fun ContactsScreen(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    Page("Contactos corporativos", onBack = { nav.popBackStack() }) {
        Field("Nombre ficticio *", state.contactName) { vm.update { copy(contactName=it) } }
        ChoiceField("Organización (ID y nombre)", state.organizations.firstOrNull { it.id.toString() == state.contactOrgId }?.let { "${it.id} · ${it.name}" } ?: "Selecciona organización", state.organizations.map { "${it.id} · ${it.name}" }) { vm.update { copy(contactOrgId=it.substringBefore(" · ")) } }
        Field("Cargo", state.contactPosition) { vm.update { copy(contactPosition=it) } }
        ChoiceField("Área", state.contactArea, listOf("Eventos", "Activaciones", "Marketing", "Comunicaciones", "Recursos Humanos", "Bienestar", "Beneficios", "Administración")) { vm.update { copy(contactArea=it) } }
        Field("Correo ficticio", state.contactEmail) { vm.update { copy(contactEmail=it) } }
        Field("Teléfono ficticio", state.contactPhone) { vm.update { copy(contactPhone=it) } }
        Field("Observaciones", state.contactNotes) { vm.update { copy(contactNotes=it) } }
        Button(onClick = vm::addContact, modifier = Modifier.fillMaxWidth()) { Text("Guardar contacto") }
        Text("Contactos registrados", style = MaterialTheme.typography.titleLarge)
        state.contacts.forEach { item -> Text("${item.name} · ${item.position} · ${item.area}\n${state.organizations.firstOrNull { org -> org.id == item.organizationId }?.name ?: "Organización ${item.organizationId}"}\n${item.email} · ${item.phone}") ; Divider() }
    }
}

@Composable
private fun FollowupsScreen(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    val user = state.currentUser
    val visible = state.followups.filter { follow -> user?.role == "SUPERVISOR" || state.opportunities.firstOrNull { it.id == follow.opportunityId }?.assignedUserId == user?.id }
    Page("Seguimientos e historial", onBack = { nav.popBackStack() }) {
        Text("Registrar nueva gestión", style = MaterialTheme.typography.titleMedium)
        ChoiceField("Oportunidad", state.opportunities.firstOrNull { it.id.toString() == state.followOpportunityId }?.let { "${it.id} · ${it.name}" } ?: "Selecciona oportunidad", state.opportunities.filter { user?.role == "SUPERVISOR" || it.assignedUserId == user?.id }.map { "${it.id} · ${it.name}" }) { vm.update { copy(followOpportunityId=it.substringBefore(" · ")) } }
        ChoiceField("Tipo de acción", state.followType, listOf("Investigación inicial", "Búsqueda de contacto", "Primer contacto", "Envío de presentación", "Envío de propuesta", "Solicitud de antecedentes", "Reunión", "Llamada", "Seguimiento", "Recepción de respuesta", "Confirmación", "Cierre")) { vm.update { copy(followType=it) } }
        Field("Resultado de la gestión *", state.followResult, minLines = 2) { vm.update { copy(followResult=it) } }
        Field("Próxima acción", state.followNextAction) { vm.update { copy(followNextAction=it) } }
        Field("Fecha próximo seguimiento (AAAA-MM-DD)", state.followNextDate) { vm.update { copy(followNextDate=it) } }
        Field("Observaciones", state.followNotes) { vm.update { copy(followNotes=it) } }
        Button(onClick = vm::addFollowUp, modifier = Modifier.fillMaxWidth()) { Text("Guardar seguimiento") }
        Text("Historial registrado", style = MaterialTheme.typography.titleLarge)
        if (visible.isEmpty()) Text("Todavía no hay seguimientos registrados.")
        visible.forEach { item ->
            val opp = state.opportunities.firstOrNull { it.id == item.opportunityId }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(opp?.name ?: "Oportunidad ${item.opportunityId}", fontWeight = FontWeight.Bold); Text("${item.dateTime} · ${item.actionType}"); Text("Resultado: ${item.result}"); if (item.nextAction.isNotBlank()) Text("Próxima acción: ${item.nextAction} (${item.nextDate})"); if (item.notes.isNotBlank()) Text(item.notes) } }
        }
    }
}

@Composable
private fun SupervisorScreen(state: AppUiState, vm: UserViewModel, nav: androidx.navigation.NavHostController) {
    if (state.currentUser?.role != "SUPERVISOR") { LaunchedEffect(Unit) { nav.popBackStack() }; return }
    Page("Panel de supervisor", onBack = { nav.popBackStack() }) {
        Text("Vista global del equipo", style = MaterialTheme.typography.titleLarge)
        Text("Ejecutivos activos: ${state.users.count { it.role == "EJECUTIVO" }} · Oportunidades: ${state.opportunities.size}")
        state.opportunities.forEach { item ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${item.status} · Prioridad ${item.priority} · ${item.eventDate}")
                ChoiceField("Responsable", state.users.firstOrNull { it.id == item.assignedUserId }?.let { "${it.id} · ${it.name}" } ?: "Sin responsable", state.users.filter { it.role == "EJECUTIVO" }.map { "${it.id} · ${it.name}" }) { selected -> vm.assign(item, selected.substringBefore(" · ").toLongOrNull() ?: item.assignedUserId) }
                ChoiceField("Estado definitivo", item.status, listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada", "Activación realizada", "Activación cancelada")) { vm.setStatus(item, it) }
            } }
        }
    }
}

@Composable
private fun Field(label: String, value: String, minLines: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(value=value, onValueChange=onChange, label={ Text(label) }, modifier=Modifier.fillMaxWidth(), minLines=minLines, singleLine=minLines == 1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember(label, value) { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded=expanded, onExpandedChange={ expanded = it }) {
        OutlinedTextField(value=value, onValueChange={}, readOnly=true, label={ Text(label) }, trailingIcon={ ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier=Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded=expanded, onDismissRequest={ expanded=false }) {
            options.forEach { option -> DropdownMenuItem(text={ Text(option) }, onClick={ onSelect(option); expanded=false }) }
        }
    }
}
