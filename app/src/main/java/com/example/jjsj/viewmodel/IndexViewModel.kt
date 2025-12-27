package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjsj.data.model.IndexData
import com.example.jjsj.data.repository.IndexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 指数ViewModel
 */
class IndexViewModel(
    private val repository: IndexRepository = IndexRepository()
) : ViewModel() {
    
    private val _domesticIndicesState = MutableStateFlow<UiState<List<IndexData>>>(UiState.Loading)
    val domesticIndicesState: StateFlow<UiState<List<IndexData>>> = _domesticIndicesState.asStateFlow()
    
    private val _globalIndicesState = MutableStateFlow<UiState<List<IndexData>>>(UiState.Loading)
    val globalIndicesState: StateFlow<UiState<List<IndexData>>> = _globalIndicesState.asStateFlow()
    
    // 为了向后兼容，保留原有的indicesState
    val indicesState: StateFlow<UiState<List<IndexData>>> = _domesticIndicesState.asStateFlow()
    
    init {
        loadIndices()
        loadGlobalIndices()
    }
    
    /**
     * 加载国内指数数据
     */
    fun loadIndices() {
        viewModelScope.launch {
            _domesticIndicesState.value = UiState.Loading
            repository.getMajorIndices()
                .onSuccess { indices ->
                    _domesticIndicesState.value = UiState.Success(indices)
                }
                .onFailure { error ->
                    _domesticIndicesState.value = UiState.Error(error.message ?: "加载失败")
                }
        }
    }
    
    /**
     * 加载全球指数数据
     */
    fun loadGlobalIndices() {
        viewModelScope.launch {
            _globalIndicesState.value = UiState.Loading
            repository.getGlobalIndices()
                .onSuccess { indices ->
                    _globalIndicesState.value = UiState.Success(indices)
                }
                .onFailure { error ->
                    _globalIndicesState.value = UiState.Error(error.message ?: "加载失败")
                }
        }
    }
    
    /**
     * 刷新指数数据
     */
    fun refreshIndices() {
        loadIndices()
        loadGlobalIndices()
    }
}

