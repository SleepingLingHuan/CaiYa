package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jjsj.data.repository.FundRepository

/**
 * FundViewModel工厂类
 */
class FundViewModelFactory(
    private val repository: FundRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FundViewModel::class.java)) {
            return FundViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

