# Guía Rápida de Instalación

## Opción 1: Usar Android Studio (MÁS FÁCIL)

1. **Descargar Android Studio**
   - Visita: https://developer.android.com/studio
   - Descarga e instala Android Studio para tu sistema operativo

2. **Abrir el proyecto**
   - Abre Android Studio
   - Selecciona "Open" o "Open an Existing Project"
   - Navega a la carpeta del proyecto `apk` y ábrela
   - Espera a que Android Studio descargue las dependencias (puede tardar unos minutos la primera vez)

3. **Ejecutar la aplicación**
   - Conecta un teléfono Android con USB (activando "Depuración USB" en las opciones de desarrollador)
   - O crea un emulador: Tools > Device Manager > Create Device
   - Presiona el botón verde "Run" (▶️) en la barra superior
   - Selecciona tu dispositivo/emulador
   - ¡Listo! La app se instalará automáticamente

## Opción 2: Instalar APK pre-compilado

Si alguien ya compiló la aplicación, simplemente:

1. Copia el archivo `app-debug.apk` a tu teléfono Android
2. Abre el archivo en tu teléfono
3. Permite la instalación de fuentes desconocidas si te lo pide
4. Instala la aplicación

## Problemas Comunes

### "Gradle sync failed"
- Ve a: File > Invalidate Caches > Invalidate and Restart
- Espera a que Android Studio reinicie y vuelva a sincronizar

### "SDK not found"
- Ve a: File > Project Structure > SDK Location
- Android Studio debería detectar automáticamente la ubicación del SDK
- Si no, descarga el SDK desde: Tools > SDK Manager

### No puedo conectar mi teléfono
1. Activa las opciones de desarrollador en tu Android:
   - Ve a Configuración > Acerca del teléfono
   - Toca 7 veces sobre "Número de compilación"
2. Activa "Depuración USB":
   - Ve a Configuración > Opciones de desarrollador
   - Activa "Depuración USB"
3. Conecta el teléfono con un cable USB
4. Acepta el diálogo de autorización en el teléfono

## Requisitos Mínimos

- **Para compilar**: Android Studio + computadora con 8GB RAM mínimo
- **Para ejecutar**: Android 7.0 (API 24) o superior

## ¿Necesitas ayuda?

Abre un issue en GitHub con tu problema y te ayudaremos.
