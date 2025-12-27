package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjsj.data.model.Transaction
import com.example.jjsj.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 交易记录ViewModel
 */
class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _clearDataState = MutableStateFlow<ClearDataState>(ClearDataState.Idle)
    val clearDataState: StateFlow<ClearDataState> = _clearDataState.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    /**
     * 加载所有交易记录
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactionList ->
                _transactions.value = transactionList
            }
        }
    }
    
    /**
     * 添加交易记录
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            _clearDataState.value = ClearDataState.Loading
            val result = repository.clearAllData()
            _clearDataState.value = if (result.isSuccess) {
                ClearDataState.Success
            } else {
                ClearDataState.Error(result.exceptionOrNull()?.message ?: "清除失败")
            }
        }
    }
    
    /**
     * 重置清除数据状态
     */
    fun resetClearDataState() {
        _clearDataState.value = ClearDataState.Idle
    }
}

/**
 * 清除数据状态
 */
sealed class ClearDataState {
    object Idle : ClearDataState()
    object Loading : ClearDataState()
    object Success : ClearDataState()
    data class Error(val message: String) : ClearDataState()
}

