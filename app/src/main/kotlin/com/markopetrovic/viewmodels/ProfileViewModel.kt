import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: LeafUserRepository, 
    private val currentUserId: String 
) : ViewModel() {

    var user: LeafAppUserDTO? by mutableStateOf(null)

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                user = userRepository.getUserByUid(currentUserId)
            } catch (e: Exception) {
                println("PROFILE_ERROR: $e")
                user = null
            }
        }
    }
}
