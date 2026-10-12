package com.example.aquacontrol.viewmodel.supervisor

sealed interface SupervisorUiState {

    object Loading : SupervisorUiState

    data class Success(
        val lineasNormales: Int,
        val lineasEnAdvertencia: Int,
        val lineasCriticas: Int,
        val lineasSinMediciones: Int
    ) : SupervisorUiState {

        val totalLineas: Int
            get() = lineasNormales +
                    lineasEnAdvertencia +
                    lineasCriticas +
                    lineasSinMediciones

        val lineasConMediciones: Int
            get() = lineasNormales +
                    lineasEnAdvertencia +
                    lineasCriticas
    }

    data class Error(
        val mensaje: String
    ) : SupervisorUiState
}