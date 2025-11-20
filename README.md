# Informes de la Congregación - App Android

Aplicación Android para llevar el control de los informes de los publicadores y la asistencia a las reuniones de la congregación.

## Características

- **Gestión de Publicadores**: Agregar, editar y administrar información de publicadores
  - Nombre, teléfono, email
  - Tipo de publicador (Publicador, Precursor Auxiliar, Regular, Especial)
  - Estado activo/inactivo

- **Informes Mensuales**: Registro completo de actividad de predicación
  - Horas de servicio
  - Publicaciones colocadas
  - Videos mostrados
  - Revisitas realizadas
  - Estudios bíblicos dirigidos
  - Comentarios adicionales

- **Gestión de Reuniones**: Control de asistencia a reuniones
  - Reuniones de fin de semana
  - Reuniones entre semana
  - Asambleas, congresos y memorial
  - Registro individual de asistencia por publicador
  - Asistencia total por reunión

- **Dashboard**: Vista general con estadísticas
  - Número total de publicadores activos
  - Informes del mes actual
  - Total de horas del mes

## Requisitos

- Android Studio Arctic Fox (2020.3.1) o superior
- Android SDK 34
- Gradle 8.1.0
- Kotlin 1.9.0
- Dispositivo Android con API 24 (Android 7.0) o superior

## Instalación y Compilación

### Opción 1: Usar Android Studio (Recomendado)

1. **Clonar o descargar el repositorio**
   ```bash
   git clone https://github.com/hugoescalda21/apk.git
   cd apk
   ```

2. **Abrir el proyecto en Android Studio**
   - Abre Android Studio
   - Selecciona "Open an Existing Project"
   - Navega a la carpeta del proyecto y selecciónala
   - Espera a que Gradle sincronice las dependencias

3. **Compilar y ejecutar**
   - Conecta un dispositivo Android con depuración USB habilitada, o
   - Crea un emulador Android desde AVD Manager
   - Haz clic en el botón "Run" (▶️) en Android Studio
   - Selecciona el dispositivo/emulador donde quieres instalar la app

### Opción 2: Compilar desde línea de comandos

1. **Preparar el entorno**
   ```bash
   # Asegúrate de tener configurada la variable ANDROID_HOME
   export ANDROID_HOME=/path/to/android/sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

2. **Compilar el APK**
   ```bash
   # Navega al directorio del proyecto
   cd apk

   # Compilar APK de debug
   ./gradlew assembleDebug

   # El APK estará en: app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Instalar en dispositivo**
   ```bash
   # Conecta tu dispositivo Android y ejecuta:
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Opción 3: Compilar APK de Release (para distribución)

1. **Crear un keystore (solo la primera vez)**
   ```bash
   keytool -genkey -v -keystore congregation-reports.keystore \
     -alias congregation-key -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configurar signing**
   - Crea un archivo `keystore.properties` en la raíz del proyecto:
   ```properties
   storePassword=tu_password
   keyPassword=tu_password
   keyAlias=congregation-key
   storeFile=congregation-reports.keystore
   ```

3. **Compilar release**
   ```bash
   ./gradlew assembleRelease
   ```

## Estructura del Proyecto

```
apk/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/congregation/reports/
│   │       │   ├── data/              # Entidades, DAOs, Database
│   │       │   ├── ui/                # Activities y Adapters
│   │       │   ├── viewmodel/         # ViewModels
│   │       │   └── utils/             # Utilidades
│   │       ├── res/
│   │       │   ├── layout/            # Diseños XML
│   │       │   ├── values/            # Strings, colores, temas
│   │       │   └── drawable/          # Recursos gráficos
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación principal
- **Room Database**: Persistencia de datos local
- **LiveData**: Observación de cambios de datos
- **ViewModel**: Gestión del ciclo de vida y datos de UI
- **Material Design 3**: Componentes de interfaz de usuario
- **RecyclerView**: Listas eficientes
- **Coroutines**: Programación asíncrona

## Uso de la Aplicación

### 1. Gestionar Publicadores

- En la pantalla principal, toca la tarjeta "Publicadores"
- Usa el botón flotante (+) para agregar un nuevo publicador
- Toca un publicador existente para ver/editar sus detalles
- Completa el formulario con la información del publicador
- Guarda los cambios

### 2. Registrar Informes

- En la pantalla principal, toca la tarjeta "Informes del Mes"
- Selecciona el mes y año deseados
- Usa el botón flotante (+) para crear un nuevo informe
- Selecciona el publicador
- Ingresa las horas, publicaciones, videos, revisitas y estudios
- Agrega comentarios si es necesario
- Guarda el informe

### 3. Gestionar Reuniones y Asistencia

- En la pantalla principal, toca la tarjeta "Reuniones"
- Usa el botón flotante (+) para crear una nueva reunión
- Selecciona el tipo de reunión y la fecha
- Ingresa la asistencia total
- Guarda la reunión
- Después de guardar, podrás registrar la asistencia individual
- Marca los publicadores presentes
- Usa "Marcar todos presentes" para agilizar el registro

## Base de Datos

La aplicación utiliza Room Database con las siguientes entidades:

### Publishers (Publicadores)
- ID, Nombre, Teléfono, Email, Tipo, Estado activo

### Reports (Informes)
- ID, PublisherID, Mes, Año, Horas, Publicaciones, Videos, Revisitas, Estudios, Comentarios

### Meetings (Reuniones)
- ID, Tipo, Fecha, Asistencia total

### Attendance (Asistencia)
- ID, PublisherID, MeetingID, Presente

## Personalización

### Cambiar colores de la aplicación
Edita `app/src/main/res/values/colors.xml`

### Modificar textos
Edita `app/src/main/res/values/strings.xml`

### Ajustar temas
Edita `app/src/main/res/values/themes.xml`

## Solución de Problemas

### Error de sincronización de Gradle
```bash
# Limpia y reconstruye el proyecto
./gradlew clean
./gradlew build
```

### Error de Room Database
Si ves errores relacionados con Room, verifica que KSP esté configurado correctamente en `build.gradle`

### Error de permisos en Linux/Mac
```bash
# Dale permisos de ejecución a gradlew
chmod +x gradlew
```

## Contribuir

Las contribuciones son bienvenidas. Por favor:
1. Crea un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## Contacto

Para preguntas o sugerencias, abre un issue en GitHub.

---

**Nota**: Esta aplicación está diseñada para uso interno de congregaciones religiosas y no almacena ni transmite datos a servidores externos. Todos los datos se guardan localmente en el dispositivo.
