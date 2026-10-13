package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aquacontrol.repository.flushing.FlushingRepository

class RegistrarFlushingViewModelFactory(
    private val repository: FlushingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrarFlushingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrarFlushingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
