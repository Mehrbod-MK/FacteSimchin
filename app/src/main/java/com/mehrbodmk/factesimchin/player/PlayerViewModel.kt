package com.mehrbodmk.factesimchin.player

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehrbodmk.factesimchin.models.PlayerPresence
import com.mehrbodmk.factesimchin.player.useCases.UseCasePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    handle : SavedStateHandle,
    val useCase : UseCasePlayer
) : ViewModel(), DefaultLifecycleObserver {

    private val _players : MutableStateFlow<List<PlayerPresence>> = MutableStateFlow(listOf())
    private val _error : MutableSharedFlow<Throwable> = MutableSharedFlow()

    val players : StateFlow<List<PlayerPresence>> = _players

    val error : SharedFlow<Throwable> = _error

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            useCase.list().collect { playersList ->
                Timber.d("=============== player ${playersList.size}")
                _players.value = playersList
            }
        }
    }

    fun addNewPlayer(name: String) {
        viewModelScope.launch {
            try {
                useCase.addNewPlayer(name).getOrThrow()
            } catch (e: Exception) {
                _error.emit(e)
            }
        }
    }
}