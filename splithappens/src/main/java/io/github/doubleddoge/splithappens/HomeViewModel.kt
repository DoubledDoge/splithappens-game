package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.java

data class HomeUiState(
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500,
    val isLoading: Boolean = true

)

class HomeViewModel(private val userDao: UserDao) : ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init{
        loadOrInitUser()
    }
    private fun loadOrInitUser(){
        viewModelScope.launch {
           userDao.observeAll().collect { users ->
               val existingUser = users.firstOrNull()

               if(existingUser != null){
                   _uiState.value = HomeUiState(
                       displayName = existingUser.displayName,
                       chipsOwned = existingUser.chipsOwned,
                       isLoading = false

                   )
               }else{
                   val newUser = UserEntity(
                       displayName = "Player 1",
                       chipsOwned = 2500

                   )
                   _uiState.value = HomeUiState(
                       displayName = newUser.displayName,
                       chipsOwned = newUser.chipsOwned,
                       isLoading = false
                   )
               }
           }

        }
    }
}
class HomeViewModelFactory(private val userDao: UserDao): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModelFactory::class.java)){
            @Suppress("UNCHECKED_CAST")
            return super.create(modelClass)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}