# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

## [1.0.0] - 2024-11-20

### Añadido
- Gestión completa de publicadores
  - Agregar, editar y eliminar publicadores
  - Tipos: Publicador, Precursor Auxiliar, Regular, Especial
  - Información de contacto (teléfono, email)
  - Estado activo/inactivo

- Sistema de informes mensuales
  - Registro de horas de servicio
  - Publicaciones y videos mostrados
  - Revisitas y estudios bíblicos
  - Comentarios adicionales
  - Filtrado por mes y año

- Gestión de reuniones y asistencia
  - Tipos: Fin de semana, Entre semana, Asamblea, Congreso, Memorial
  - Registro de asistencia total
  - Control individual de asistencia por publicador
  - Función de marcar todos presentes

- Dashboard principal
  - Contador de publicadores activos
  - Informes y horas del mes actual
  - Navegación rápida a todas las secciones

- Base de datos local con Room
  - Persistencia de datos en el dispositivo
  - Sin conexión a internet requerida
  - Relaciones entre tablas (Foreign Keys)

- Interfaz de usuario moderna
  - Material Design 3
  - Tema claro y oscuro
  - Diseño responsive
  - Navegación intuitiva

### Características técnicas
- Arquitectura MVVM (Model-View-ViewModel)
- LiveData para observación reactiva de datos
- Coroutines para operaciones asíncronas
- ViewBinding para acceso seguro a vistas
- Repository pattern para abstracción de datos

## [Próximas versiones]

### Planeado para v1.1.0
- Exportar informes a PDF
- Estadísticas y gráficos
- Respaldo y restauración de datos
- Compartir informes por email/WhatsApp

### Ideas para futuras versiones
- Sincronización en la nube (opcional)
- Recordatorios para entregar informes
- Múltiples congregaciones en un dispositivo
- Modo tablet con vista de dos paneles
- Búsqueda avanzada de publicadores
- Historial de cambios en informes
