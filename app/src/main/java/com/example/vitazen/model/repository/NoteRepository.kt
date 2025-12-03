package com.example.vitazen.model.repository

import com.example.vitazen.model.dao.NoteDao
import com.example.vitazen.model.data.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    
    fun getNotesByUserId(userId: String): Flow<List<Note>> {
        return noteDao.getNotesByUserId(userId)
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)
    }

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotesByUserId(userId: String) {
        noteDao.deleteAllNotesByUserId(userId)
    }
}
