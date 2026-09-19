package com.example.ui.screens.topology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.di.AppModule
import com.example.model.NetworkNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkTopologyViewModel(
    private val repository: DataRepository = AppModule.getRepository()
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    val nodes: StateFlow<List<NetworkNode>> = _nodes.asStateFlow()

    init {
        observeNodes()
    }

    private fun observeNodes() {
        viewModelScope.launch {
            repository.getNetworkNodesStream().collect {
                _nodes.value = it
            }
        }
    }
}
