# AquaControl 4.0 — Android App

Aplicación móvil desarrollada en **Android Studio**, utilizando **Kotlin, Jetpack Compose, MVVM, Repository Pattern y DataSources locales/remotos**.

Este proyecto corresponde a la **Evaluación Aplicada 2 (EA2)** del módulo **Desarrollo de Aplicaciones Móviles — DUOC UC**.

La versión **4.0** representa una actualización mayor respecto a la versión 3.x, incorporando mejoras en arquitectura, navegación, estructura del proyecto, configuración de Gradle y un módulo de **Flushing completamente refactorizado**.

---

## 🆕 ¿Qué trae AquaControl 4.0?

### ✔ Cambios mayores respecto a 3.x

1. **Migración completa del módulo remoto de Flushing a Kotlin**

   `FlushingRemoteDataSource.java` → `FlushingRemoteDataSource.kt`

2. **Navegación Compose reorganizada y modularizada**

   Nuevos `NavHost` por feature, rutas corregidas e integración del flujo de Flushing.

3. **Estructura de datos actualizada**

   DTOs revisados, repositorios reorganizados y DataSources especializados.

4. **Configuración del proyecto modernizada**

   * `build.gradle.kts` optimizado.
   * `settings.gradle.kts` corregido.
   * Catálogo de versiones mediante `libs.versions.toml`.

5. **Arquitectura organizada por features**

   Carpetas reorganizadas para representar los dominios funcionales de la aplicación.

6. **Documentación técnica actualizada**

   README, estructura del proyecto y documentación de la arquitectura MVVM.

---

## 📱 Funcionalidades principales

### Gestión de roles

* Operario.
* Supervisor.
* Pantallas de bienvenida según el rol seleccionado.

### Navegación jerárquica

* Granjas.
* Galpones.
* Líneas de bebederos.
* Detalle de línea.

### Monitoreo térmico

Visualización del estado térmico de las líneas:

* 🟢 **Normal**
* 🟡 **Advertencia**
* 🔴 **Crítico**

### 💧 Módulo Flushing 4.0

* Simulación de limpieza térmica.
* Registro de flushing.
* Detalle de flushing.
* ViewModels dedicados.
* DataSources local y remoto.
* Repositorio especializado.

### Arquitectura y UI

* Componentes UI reutilizables mediante Jetpack Compose.
* ViewModels con `StateFlow`.
* Repository Pattern.
* DataSources locales y remotos.
* Arquitectura MVVM.
* Navegación mediante Navigation Compose.

---

## 🎨 Prototipo UI

Prototipo desarrollado en Canva:

👉 **[Ver prototipo de AquaControl](https://vistausariofullstack.my.canva.site/aquacontrol-2-0-x-arizt-a)**

---

## 🚀 Tecnologías utilizadas

| Tecnología             | Uso                                   |
| ---------------------- | ------------------------------------- |
| **Kotlin**             | Lenguaje principal                    |
| **Jetpack Compose**    | Desarrollo de UI declarativa          |
| **Material 3**         | Componentes y sistema de diseño       |
| **MVVM**               | Arquitectura de presentación          |
| **StateFlow**          | Gestión reactiva del estado           |
| **Navigation Compose** | Navegación declarativa                |
| **Room**               | Persistencia local                    |
| **Retrofit**           | Acceso remoto                         |
| **Repository Pattern** | Abstracción y gestión de datos        |
| **Gradle Kotlin DSL**  | Configuración del proyecto            |
| **libs.versions.toml** | Catálogo centralizado de dependencias |

---

## 🏗️ Arquitectura

AquaControl 4.0 implementa una arquitectura basada en **MVVM + Repository Pattern + DataSources**, separando las responsabilidades de presentación, lógica y acceso a datos.

```text
UI — Jetpack Compose
        │
        ▼
ViewModel — StateFlow
        │
        ▼
Repository
        │
   ┌────┴────┐
   ▼         ▼
Local      Remote
DataSource DataSource
   │         │
   └────┬────┘
        ▼
 Modelos / DTOs
```

### Principios aplicados

* **Single Activity Architecture**
* **MVVM**
* **Navegación modular por features**
* **Repositorios desacoplados**
* **DTOs y modelos por dominio**
* **DataSources especializados**
* **StateFlow para gestión reactiva del estado**
* **Gradle Kotlin DSL**
* **Version Catalog mediante `libs.versions.toml`**

---

## 📁 Estructura del proyecto

La estructura de AquaControl 4.0 se organiza por responsabilidades y dominios funcionales.

```text
## 📁 Estructura del proyecto AquaControl

```text
AquaControl/
└── app/
    └── src/
        ├── androidTest/
        │   └── java/com/example/aquacontrol/
        │       └── ExampleInstrumentedTest.kt
        |       │     └── java/com/example/aquacontrol/
        │       ├── MainActivity.kt [RIMSKY]
        │       │
        │       ├── data/
        │       │   ├── AppDatabase.kt [RIMSKY]
        │       │   ├── flushing/
        │       │   │   ├── FlushingDataSource.kt [RIMSKY]
        │       │   │   ├── FotosFlushing.kt [CHRIS]
        │       │   │   ├── local/
        │       │   │   │   ├── FlushingDao.kt [RIMSKY]
        │       │   │   │   ├── FlushingEntity.kt [RIMSKY]
        │       │   │   │   └── FlushingLocalDataSource.kt [RIMSKY]
        │       │   │   └── remote/
        │       │   │       ├── FlushingApiClient.kt [RIMSKY]
        │       │   │       ├── FlushingApiService.kt [RIMSKY]
        │       │   │       └── FlushingRemoteDataSource.kt [RIMSKY]
        │       │   ├── simulacion/
        │       │   │   └── GeneradorTemperatura.kt [CHRIS]
        │       │   └── temperatura/
        │       │       └── local/
        │       │           ├── MedicionTemperaturaDao.kt [CHRIS]
        │       │           ├── MedicionTemperaturaEntity.kt [CHRIS]
        │       │           └── TemperaturaLocalDataSource.kt [CHRIS]
        │       │
        │       ├── iu/
        │       │   ├── alertas/ [RIMSKY]
        │       │   │   ├── AlertasScreen.kt
        │       │   │   └── DetalleAlertaScreen.kt
        │       │   ├── components/
        │       │   │   ├── AquaEncabezado.kt [CHRIS]
        │       │   │   ├── AquaPanelResumen.kt [CHRIS]
        │       │   │   ├── AquaTarjetaAccion.kt [CHRIS]
        │       │   │   ├── AquaTarjetaRol.kt [CHRIS]
        │       │   │   ├── BottomBar.kt [RIMSKY]
        │       │   │   ├── IndicadorEstado.kt [CHRIS]
        │       │   │   └── ReusableButton.kt [RIMSKY]
        │       │   ├── detalle/ [RIMSKY]
        │       │   │   └── DetalleLineaScreen.kt
        │       │   ├── flushing/
        │       │   │   ├── CalendarioFlushing.kt [RIMSKY]
        │       │   │   ├── FlushingDetalleScreen.kt [CHRIS]
        │       │   │   ├── FlushingHomeScreen.kt [RIMSKY]
        │       │   │   ├── FlushingScreen.kt [RIMSKY]
        │       │   │   ├── FotoFlushing.kt [CHRIS]
        │       │   │   └── RegistrarFlushingScreen.kt [RIMSKY]
        │       │   ├── galpones/ [RIMSKY]
        │       │   │   └── GalponScreen.kt
        │       │   ├── granjas/ [RIMSKY]
        │       │   │   └── GranjaScreen.kt
        │       │   ├── historial/ [CHRIS]
        │       │   │   ├── FiltrosTemperaturaPanel.kt
        │       │   │   └── HistorialTemperaturaScreen.kt
        │       │   ├── lineas/ [RIMSKY]
        │       │   │   └── LineaScreen.kt
        │       │   ├── navigation/ [RIMSKY]
        │       │   │   ├── AppNavHost.kt
        │       │   │   └── Routes.kt
        │       │   ├── roles/ [RIMSKY]
        │       │   │   ├── OperarioScreen.kt
        │       │   │   ├── RolSelectionScreen.kt
        │       │   │   └── SupervisorScreen.kt
        │       │   ├── simulacion/ [CHRIS]
        │       │   │   └── SimulacionScreen.kt
        │       │   └── temperatura/ [CHRIS]
        │       │       ├── DetalleTemperaturaScreen.kt
        │       │       ├── GraficoTemperatura.kt
        │       │       └── RegistrarTemperaturaScreen.kt 
        │       │
        │       ├── model/
        │       │   ├── estado/EstadoLinea.kt [CHRIS]
        │       │   ├── flushing/
        │       │   │   ├── EventoFlushing.kt [CHRIS]
        │       │   │   ├── FechasCalendarioFlushing.kt [CHRIS]
        │       │   │   └── FlushingDTO.kt [RIMSKY]
        │       │   ├── galpon/Galpon.kt [CHRIS]
        │       │   ├── granja/Granja.kt [CHRIS]
        │       │   ├── historial/HistorialTemperatura.kt [CHRIS]
        │       │   ├── linea/
        │       │   │   ├── LineaBebedero.kt [CHRIS]
        │       │   │   └── LineaMonitoreada.kt [CHRIS]
        │       │   ├── perfil/PerfilUsuario.kt [CHRIS]
        │       │   ├── simulacion/EscenarioSimulacion.kt [CHRIS]
        │       │   └── temperatura/
        │       │       ├── FechasHistorialTemperatura.kt [CHRIS]
        │       │       ├── FiltroHistorialTemperatura.kt [CHRIS]
        │       │       ├── MedicionTemperatura.kt [CHRIS]
        │       │       └── OrigenMedicion.kt [CHRIS]
        │       │
        │       ├── notificaciones/
        │       │   └── NotificadorAlertas.kt [CHRIS]
        │       │
        │       ├── repository/
        │       │   ├── bebedero/
        │       │   │   └── BebederoRepository.kt [CHRIS]
        │       │   ├── data/
        │       │   │   └── FlushingRemoteRepository.kt [RIMSKY]
        │       │   ├── flushing/
        │       │   │   ├── FlushingRepository.kt [RIMSKY]
        │       │   │   └── FlushingRepositoryImpl.kt [RIMSKY]
        │       │   └── temperatura/
        │       │       ├── TemperaturaRepository.kt [CHRIS]
        │       │       └── TemperaturaRepositoryImpl.kt [CHRIS]
        │       │
        │       ├── ui/theme/
        │       │   ├── Color.kt [RIMSKY]
        │       │   ├── Theme.kt [RIMSKY]
        │       │   └── Type.kt [RIMSKY]
        │       │
        │       └── viewmodel/
        │           ├── alertas/AlertasViewModel.kt [CHRIS]
        │           ├── detalle/
        │           │   ├── DetalleLineaUiState.kt [CHRIS]
        │           │   └── DetalleLineaViewModel.kt [CHRIS]
        │           ├── flushing/
        │           │   ├── DetalleFlushingUiState.kt
        │           │   ├── DetalleFlushingViewModel.kt
        │           │   ├── FlushingUiState.kt [CHRIS]
        │           │   ├── FlushingViewModel.kt [CHRIS]
        │           │   ├── RegistrarFlushingUiState.kt [CHRIS]
        │           │   ├── RegistrarFlushingViewModel.kt [CHRIS]
        │           │   ├── RegistrarFlushingViewModelFactory.kt [RIMSKY]
        │           │   └── SeleccionFlushingViewModel.kt [CHRIS]
        │           ├── galpones/
        │           │   ├── GalponUiState.kt [CHRIS]
        │           │   └── GalponViewModel.kt [CHRIS]
        │           ├── granjas/
        │           │   ├── GranjaUiState.kt [CHRIS]
        │           │   └── GranjaViewModel.kt [CHRIS]
        │           ├── historial/
        │           │   ├── HistorialTemperaturaUiState.kt [CHRIS]
        │           │   └── HistorialTemperaturaViewModel.kt [CHRIS]
        │           ├── lineas/
        │           │   ├── LineaUiState.kt [CHRIS]
        │           │   └── LineasViewModel.kt [CHRIS]
        │           ├── perfil/PerfilViewModel.kt [CHRIS]
        │           ├── simulacion/
        │           │   ├── SimulacionUiState.kt [CHRIS]
        │           │   └── SimulacionViewModel.kt [CHRIS]
        │           ├── supervisor/
        │           │   ├── SupervisorUiState.kt [CHRIS]
        │           │   └── SupervisorViewModel.kt [CHRIS]
        │           └── temperatura/ [CHRIS]
        │               ├── DetalleTemperaturaUiState.kt
        │               ├── DetalleTemperaturaViewModel.kt
        │               ├── GraficoTemperaturaUiState.kt
        │               ├── GraficoTemperaturaViewModel.kt
        │               ├── RegistrarTemperaturaUiState.kt 
        │               └── RegistrarTemperaturaViewModel.kt         │
        └── test/
            └── java/com/example/aquacontrol/
                └── ExampleUnitTest.kt

```



```

---

## 🧭 Navegación

### Rutas principales

```text
roles
├── operario
└── supervisor

granjas
└── galpones/{granjaId}
    └── lineas/{galponId}
        └── detalle/{lineaId}

flushing
```

### Flujo general

```text
Selección de rol
       │
       ├── Operario
       │
       └── Supervisor
              │
              ▼
           Granjas
              │
              ▼
           Galpones
              │
              ▼
      Líneas de bebederos
              │
              ▼
       Detalle de línea
              │
              ▼
           Flushing
```

---

## 🔥 Módulo Flushing 4.0

El módulo de Flushing fue completamente refactorizado en esta versión.

### Componentes principales

* Pantalla principal de Flushing.
* Registro de Flushing.
* Detalle de Flushing.
* `FlushingViewModel`.
* `RegistrarFlushingViewModel`.
* `FlushingLocalDataSource`.
* `FlushingRemoteDataSource`.
* `FlushingRepository`.
* `FlushingRepositoryImpl`.

### Migración a Kotlin

Una de las modificaciones principales de la versión 4.0 fue la migración del DataSource remoto:

```text
FlushingRemoteDataSource.java
              │
              ▼
FlushingRemoteDataSource.kt
```

Esto permite mantener el módulo de Flushing completamente integrado con el resto de la arquitectura Kotlin del proyecto.

---

## 📊 Gestión del estado

La aplicación utiliza **StateFlow** para representar y observar estados reactivos desde los ViewModels.

Ejemplo conceptual:

```text
ViewModel
    │
    ▼
MutableStateFlow
    │
    ▼
StateFlow
    │
    ▼
Jetpack Compose UI
```

Cada feature mantiene sus propios estados y ViewModels, permitiendo separar la lógica de presentación y reducir el acoplamiento entre pantallas.

---

## 🛠️ Instalación

Clonar el repositorio:

```bash
git clone https://github.com/tuusuario/AquaControl.git
```

Luego:

1. Abrir el proyecto en **Android Studio**.
2. Sincronizar **Gradle**.
3. Esperar a que finalice la descarga de dependencias.
4. Seleccionar un emulador o dispositivo Android.
5. Ejecutar la aplicación.

---

## 👥 Autores

### Rimsky Farías Soto

**UI · Navegación · Arquitectura · Documentación 4.0**

### Christian Quiroz Roa

**Modelos · Repository · ViewModels · Documentación MVVM**

---

## 🎓 Contexto académico

Proyecto desarrollado para la **Evaluación Aplicada 2 (EA2)**.

| Campo           | Información                        |
| --------------- | ---------------------------------- |
| **Módulo**      | Desarrollo de Aplicaciones Móviles |
| **Institución** | DUOC UC                            |
| **Proyecto**    | AquaControl 4.0                    |
| **Versión**     | 4.0                                |
``
---

## 📄 Licencia

Proyecto académico desarrollado para fines educativos y **no destinado a uso comercial**.
