package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "embedded_notes")
data class EmbeddedNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val codeSnippet: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface EmbeddedNoteDao {
    @Query("SELECT * FROM embedded_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<EmbeddedNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: EmbeddedNote)

    @Update
    suspend fun updateNote(note: EmbeddedNote)

    @Delete
    suspend fun deleteNote(note: EmbeddedNote)

    @Query("DELETE FROM embedded_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Database(entities = [EmbeddedNote::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun embeddedNoteDao(): EmbeddedNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "embedded_c_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class EmbeddedNoteRepository(private val dao: EmbeddedNoteDao) {
    val allNotes: Flow<List<EmbeddedNote>> = dao.getAllNotes()

    suspend fun insert(note: EmbeddedNote) = dao.insertNote(note)

    suspend fun update(note: EmbeddedNote) = dao.updateNote(note)

    suspend fun delete(note: EmbeddedNote) = dao.deleteNote(note)

    suspend fun deleteById(id: Int) = dao.deleteNoteById(id)
}
