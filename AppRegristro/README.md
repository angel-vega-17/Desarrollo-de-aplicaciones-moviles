# Gestión móvil de oportunidades y activaciones corporativas

Aplicación académica Android desarrollada con Kotlin, Jetpack Compose, Material Design 3, Navigation Compose, Room/SQLite y MVVM (ViewModel + StateFlow). Utiliza exclusivamente datos ficticios.

## Funcionalidades implementadas
- Inicio y cierre de sesión local con roles Ejecutivo Comercial y Supervisor Comercial.
- Cuatro cuentas de demostración creadas automáticamente en el primer inicio.
- Dashboard con indicadores básicos y navegación entre módulos.
- Listado de oportunidades, búsqueda por nombre y filtro por estado.
- Creación, edición y detalle de oportunidades; validaciones de fechas, público esperado, costo, ubicación y relaciones.
- Permisos para limitar oportunidades por responsable y reservar la confirmación/descarte definitivo al supervisor.
- Registro y consulta de organizaciones y contactos ficticios.
- Registro e historial de seguimientos.
- Panel de supervisor para consultar oportunidades y reasignar responsables.
- Persistencia local Room/SQLite con entidades relacionadas: Usuario, Organización, Contacto, Oportunidad y Seguimiento.
- Captura de fotografía con cámara del sistema y vista previa asociada a la oportunidad.
- Apertura del calendario del dispositivo para programar un evento.
- Componentes Material 3, estados vacíos y animaciones de retroalimentación.

## Cuentas ficticias
Contraseña de todas las cuentas: `demo123`

| Rol | Correo |
|---|---|
| Ejecutivo Comercial | `ejecutivo1@demo.cl` |
| Ejecutivo Comercial | `ejecutivo2@demo.cl` |
| Ejecutivo Comercial | `ejecutivo3@demo.cl` |
| Supervisor Comercial | `supervisor@demo.cl` |

## Cómo ejecutar
1. Abrir la carpeta `AppRegristro` desde Android Studio.
2. Usar JDK 17 y permitir que Gradle sincronice las dependencias.
3. Ejecutar en un emulador o dispositivo Android (API 26 o superior).
4. Iniciar sesión con una de las cuentas ficticias indicadas.

## Arquitectura
- `data/Models.kt`: entidades Room y relaciones.
- `data/AppDao.kt`: consultas y operaciones de persistencia.
- `data/AppDatabase.kt`: base de datos local.
- `ui/UserViewModel.kt`: lógica, validaciones, permisos y estado mediante StateFlow.
- `ui/AppNavigation.kt`: navegación y pantallas Compose.
- `ui/theme/`: tema de Material Design 3.

## Limitaciones conocidas / revisión antes de entrega
- La base local utiliza `fallbackToDestructiveMigration()` durante esta etapa académica; un cambio de esquema puede reiniciar los datos locales.
- Las credenciales son cuentas de demostración en texto plano, no apropiadas para una aplicación productiva.
- La integración de calendario abre la aplicación de calendario del dispositivo; el usuario confirma allí la creación del evento.
- Revisar en Android Studio el Gradle Sync y ejecutar en un dispositivo/emulador antes de entregar. No se declara como verificada una compilación que no haya sido ejecutada.
- La interfaz de creación utiliza selectores simples y fechas como texto `AAAA-MM-DD`; validar las fechas ingresadas es parte de la prueba manual.

## GitHub
Desde la carpeta `AppRegristro`, ejecutar:

```bash
git add .
git commit -m "feat: implementar gestion de oportunidades comerciales"
# Si aún no se configuró el remoto:
git branch -M main
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
# Si origin ya existe, omitir git remote add y usar git push -u origin main
```
