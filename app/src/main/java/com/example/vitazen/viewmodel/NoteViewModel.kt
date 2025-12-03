package com.example.vitazen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.Note
import com.example.vitazen.model.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

class NoteViewModel(
    private val noteRepository: NoteRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                noteRepository.getNotesByUserId(currentUser.uid).collect { notes ->
                    _uiState.value = _uiState.value.copy(
                        notes = notes,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addNote(title: String, content: String, year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month - 1, day, hour, minute, 0)
                    
                    val note = Note(
                        userId = currentUser.uid,
                        title = title,
                        content = content,
                        timestamp = calendar.timeInMillis,
                        year = year,
                        month = month,
                        day = day,
                        hour = hour,
                        minute = minute
                    )
                    noteRepository.insertNote(note)
                    Toast.makeText(context, "Đã thêm ghi chú", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi thêm ghi chú: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note)
                Toast.makeText(context, "Đã cập nhật ghi chú", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi cập nhật ghi chú: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi xóa ghi chú: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
