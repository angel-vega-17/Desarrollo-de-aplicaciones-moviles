# App Registro

Compose + Material 3 · Navigation · MVVM (ViewModel + StateFlow) · Room (KSP) · Cámara (FileProvider + TakePicture) · Coil · AnimatedVisibility.

## Cómo ejecutar
1. Android Studio → File → Open → carpeta `AppRegistro`.
2. Esperar el Gradle Sync (requiere internet la primera vez).
3. Run en un emulador (Camera → Back: VirtualScene) o un dispositivo.

## Estructura
- `data/` Entity, DAO, Database, Repository
- `ui/` UserViewModel (UiState + StateFlow), AppNavigation, pantallas
- `ui/theme/` tema Material 3
- `res/xml/file_paths.xml` + `<provider>` en el Manifest (FileProvider)
