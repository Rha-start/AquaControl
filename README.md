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
AquaControl/
│
│── com/example/aquacontrol/
│   ├── data/
│   │   ├── flushing/                         ← RIMSKY
│   │   │   ├── local/
│   │   │   │   ├── FlushingDao.kt
│   │   │   │   ├── FlushingEntity.kt
│   │   │   │   └── FlushingLocalDataSource.kt
│   │   │   ├── remote/
│   │   │   │   ├── FlushingApiClient.kt
│   │   │   │   ├── FlushingApiService.kt
│   │   │   │   └── FlushingRemoteDataSource.kt
│   │   │   ├── FlushingDataSource.kt
│   │   │   ├── FlushingDTO.kt
│   │   │   ├── FotosFlushing.kt              ← CHRIS
│   │   │   └── simulacion/
│   │   │       └── GeneradorTemperatura.kt   ← CHRIS
│   │   │
│   │   ├── temperatura/                      ← CHRIS
│   │   │   ├── local/
│   │   │   │   ├── MedicionTemperaturaDao.kt
│   │   │   │   ├── MedicionTemperaturaEntity.kt
│   │   │   │   └── TemperaturaLocalDataSource.kt
│   │   │   └── TemperaturaDataSource.kt
│   │   │
│   │   ├── AppDatabase.kt                    ← RIMSKY
│   │   │
│   │   └── repository/
│   │       ├── bebedero/                     ← CHRIS
│   │       │   └── BebederoRepository.kt
│   │       │
│   │       ├── data/                         ← RIMSKY
│   │       │   └── FlushingRemoteRepository.kt
│   │       │
│   │       ├── flushing/                     ← RIMSKY
│   │       │   ├── FlushingRepository.kt
│   │       │   └── FlushingRepositoryImpl.kt
│   │       │
│   │       └── temperatura/                  ← CHRIS
│   │           ├── TemperaturaRepository.kt
│   │           └── TemperaturaRepositoryImpl.kt
│
│
│   ├── iu/
│   │   ├── alertas/                           ← RIMSKY
│   │   │   ├── AlertasScreen.kt
│   │   │   └── DetalleAlertaScreen.kt
│   │   │
│   │   ├── components/                        ← CHRIS
│   │   │   ├── AquaEncabezado.kt
│   │   │   ├── AquaPanelResumen.kt
│   │   │   ├── AquaTarjetaAccion.kt
│   │   │   ├── AquaTarjetaRol.kt
│   │   │   ├── BottomBar.kt                  ← RIMSKY
│   │   │   ├── IndicadorEstado.kt
│   │   │   └── ReusableButton.kt             ← RIMSKY

│   │   │
│   │   ├── detalle/                           ← RIMSKY
│   │   │   └── DetalleLineaScreen.kt
│   │   │
│   │   ├── flushing/                          ← RIMSKY
│   │   │   ├── CalendarioFlushing.kt
│   │   │   ├── FlushingDetalleScreen.kt      ← CHRIS
│   │   │   ├── FlushingHomeScreen.kt         
│   │   │   ├── FlushingScreen.kt
│   │   │   ├── FotoFlushing.kt              ← CHRIS
│   │   │   └── RegistrarFlushingScreen.kt
│   │   │
│   │   ├── galpones/                          ← RIMSKY
│   │   │   └── GalponScreen.kt
│   │   │
│   │   ├── granjas/                           ← RIMSKY
│   │   │   └── GranjaScreen.kt
│   │   │
│   │   ├── historial/                         ← CHRIS
│   │   │   ├── FiltrosTemperaturaPanel.kt
│   │   │   └── HistorialTemperaturaScreen.kt
│   │   │
│   │   ├── lineas/                            ← RIMSKY
│   │   │   └── LineaScreen.kt
│   │   │
│   │   ├── navigation/                        ← RIMSKY
│   │   │   ├── AppNavHost.kt
│   │   │   └── Routes.kt
│   │   │
│   │   ├── roles/                             ← RIMSKY
│   │   │   ├── OperarioScreen.kt
│   │   │   ├── RolSelectionScreen.kt
│   │   │   └── SupervisorScreen.kt
│   │   │
│   │   ├── temperatura/                       ← CHRIS
│   │   │   └── RegistrarTemperaturaScreen.kt
│   │   │
│   │   └── simulacion/                        ← CHRIS
│   │       └── SimulacionScreen.kt
│
│
│   ├── model/
│   │   ├── estado/                            ← CHRIS
│   │   │   └── EstadoLinea.kt
│   │   │
│   │   ├── flushing/                          ← CHRIS
│   │   │   ├── EventoFlushing.kt
│   │   │   ├── FechasCalendarioFlushing.kt
│   │   │   └── FlushingDTO.kt                ← RIMSKY
│   │   │
│   │   ├── galpon/                            ← CHRIS
│   │   │   └── Galpon.kt
│   │   │
│   │   ├── granja/                            ← CHRIS
│   │   │   └── Granja.kt
│   │   │
│   │   ├── historial/                         ← CHRIS
│   │   │   └── HistorialTemperatura.kt
│   │   │
│   │   ├── linea/                             ← CHRIS
│   │   │   ├── LineaBebedero.kt
│   │   │   └── LineaMonitoreada.kt
│   │   │
│   │   ├── perfil/                            ← CHRIS
│   │   │   └── PerfilUsuario.kt
│   │   │
│   │   ├── simulacion/                        ← CHRIS
│   │   │   └── EscenarioSimulacion.kt
│   │   │
│   │   └── temperatura/                       ← CHRIS
│   │       ├── FechasHistorialTemperatura.kt
│   │       ├── FiltroHistorialTemperatura.kt
│   │       ├── MedicionTemperatura.kt
│   │       └── OrigenMedicion.kt
│
│
│   ├── notificaciones/                        ← CHRIS
│   │   └── NotificadorAlertas.kt
│
│
│   ├── repository/
│   │   ├── bebedero/                          ← CHRIS
│   │   │   └── BebederoRepository.kt
│   │   │
│   │   ├── data/                              ← RIMSKY
│   │   │   └── FlushingRemoteRepository.kt
│   │   │
│   │   ├── flushing/                          ← RIMSKY
│   │   │   ├── FlushingRepository.kt
│   │   │   └── FlushingRepositoryImpl.kt
│   │   │
│   │   └── temperatura/                       ← CHRIS
│   │       ├── TemperaturaRepository.kt
│   │       └── TemperaturaRepositoryImpl.kt
│
│
│   ├── viewmodel/
│   │   ├── alertas/                           ← CHRIS
│   │   │   └── AlertasViewModel.kt
│   │   │
│   │   ├── detalle/                           ← CHRIS
│   │   │   ├── DetalleLineaUiState.kt
│   │   │   └── DetalleLineaViewModel.kt
│   │   │
│   │   ├── flushing/                          ← CHRIS
│   │   │   ├── FlushingUiState.kt
│   │   │   ├── FlushingViewModel.kt
│   │   │   ├── RegistrarFlushingUiState.kt
│   │   │   ├── RegistrarFlushingViewModel.kt
│   │   │
│   │   ├── galpones/                          ← CHRIS
│   │   │   ├── GalponUiState.kt
│   │   │   └── GalponViewModel.kt
│   │   │
│   │   ├── granjas/                           ← CHRIS
│   │   │   ├── GranjaUiState.kt
│   │   │   └── GranjaViewModel.kt
│   │   │
│   │   ├── historial/                         ← CHRIS
│   │   │   ├── HistorialTemperaturaUiState.kt
│   │   │   └── HistorialTemperaturaViewModel.kt
│   │   │
│   │   ├── lineas/                            ← CHRIS
│   │   │   ├── LineasViewModel.kt
│   │   │   └── LineaUiState.kt
│   │   │
│   │   ├── perfil/                            ← CHRIS
│   │   │   └── PerfilViewModel.kt
│   │   │
│   │   ├── simulacion/                        ← CHRIS
│   │   │   ├── SimulacionUiState.kt
│   │   │   └── SimulacionViewModel.kt
│   │   │
│   │   ├── supervisor/                        ← CHRIS
│   │   │   ├── SupervisorUiState.kt
│   │   │   └── SupervisorViewModel.kt
│   │   │
│   │   └── temperatura/                       ← CHRIS
│   │       ├── RegistrarTemperaturaUiState.kt
│   │       └── RegistrarTemperaturaViewModel.kt
│
│
│   └── MainActivity.kt                        ← RIMSKY


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
