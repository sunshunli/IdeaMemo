package com.ldlywt.note.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ldlywt.note.bean.Note
import com.ldlywt.note.bean.NoteShowBean
import com.ldlywt.note.bean.NoteTagCrossRef
import com.ldlywt.note.bean.Reminder
import com.ldlywt.note.bean.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagNoteDao {
    @Transaction
    @RawQuery(
        observedEntities = [
            Note::class,
            Tag::class,
            Reminder::class,
            NoteTagCrossRef::class,
        ]
    )
    fun rawGetQueryFlow(query: SimpleSQLiteQuery): Flow<List<NoteShowBean>>

    fun getAll(sortTime: String, order: String): Flow<List<NoteShowBean>> {
        return rawGetQueryFlow(
            SimpleSQLiteQuery("SELECT * FROM Note order by $sortTime $order")
        )
    }

    @Transaction
    @Query("SELECT * FROM Note order by update_time desc")
    fun getAllNoteByUpdateTime(): Flow<List<NoteShowBean>>

    @Transaction
    @Query("SELECT * FROM Note order by create_time desc")
    fun getAllNoteByCreateTime(): Flow<List<NoteShowBean>>

    @Transaction
    @Query("SELECT * FROM Note order by update_time desc")
    fun getAllNoteWithTagList(): List<NoteShowBean>

    @Transaction
    @Query("SELECT * FROM Note ORDER BY RANDOM()")
    fun getAllRandom(): Flow<List<NoteShowBean>>

    @Transaction
    @Query(
        "SELECT COUNT(*) FROM Note n1 join NoteTagCrossRef ncr on " + "n1.note_id=ncr.note_id where ncr.tag=:tagName order" + " by n1.update_time desc"
    )
    fun countNoteListWithByTag(tagName: String): Int

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM Note 
        INNER JOIN NoteTagCrossRef ON Note.note_id = NoteTagCrossRef.note_id 
        WHERE NoteTagCrossRef.tag = :tagName
        order by Note.update_time desc
        """
    )
    fun getNoteListWithByTag(tagName: String): Flow<List<NoteShowBean>>

    @Query(
        """
        SELECT * FROM Tag 
        INNER JOIN NoteTagCrossRef ON Tag.tag = NoteTagCrossRef.tag 
        WHERE NoteTagCrossRef.note_id = :noteId
        """
    )
    fun getTagListByNoteId(noteId: Long): Flow<List<Tag>>

    @Transaction
    @Query("SELECT * FROM Note WHERE note_id = :noteId")
    fun getNoteShowBeanById(noteId: Long): NoteShowBean?

    @Transaction
    @Query("SELECT * FROM Note WHERE date(create_time / 1000, 'unixepoch') = :selectedDate")
    fun getNoteShowOnDate(selectedDate: String): List<NoteShowBean>

    @Transaction
    @Query("SELECT DISTINCT strftime('%Y', datetime(create_time/1000, 'unixepoch')) AS year FROM Note ORDER BY year DESC")
    suspend fun getAllDistinctYears(): List<String>

    @Transaction
    @Query("SELECT * FROM Note WHERE strftime('%Y', datetime(create_time/1000, 'unixepoch')) = :year")
    fun getNotesByYear(year: String): Flow<List<NoteShowBean>>

    @Query("SELECT * FROM note WHERE create_time BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY create_time DESC")
    fun getNotesByCreateTimeRange(startTime: Long, endTime: Long): Flow<List<NoteShowBean>>

}