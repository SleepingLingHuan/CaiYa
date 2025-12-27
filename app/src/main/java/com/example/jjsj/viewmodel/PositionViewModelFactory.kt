package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jjsj.data.repository.PositionRepository
import com.example.jjsj.data.repository.FundRepository

/**
 * PositionViewModel工厂类
 */
class PositionViewModelFactory(
    private val positionRepository: PositionRepository,
    private val fundRepository: FundRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PositionViewModel::class.java)) {
            return PositionViewModel(positionRepository, fundRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

