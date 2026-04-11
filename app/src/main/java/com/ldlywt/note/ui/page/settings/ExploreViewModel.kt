package com.ldlywt.note.ui.page.settings

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.bean.Note
import com.ldlywt.note.db.repo.TagNoteRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tagNoteRepo: TagNoteRepo
) : ViewModel() {

    // 关键：由 ViewModel 持有状态，防止跳转返回重置
    val shuffledList = mutableStateListOf<Note>()
    var totalCount = mutableIntStateOf(0)

    init {
        initDailyNotes()
    }

    private fun initDailyNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            // 只在第一次加载时获取，不监听流
            val allNotes = tagNoteRepo.queryAllNoteList()
            if (allNotes.isNotEmpty()) {
                val seed = LocalDate.now().toEpochDay()
                val random = Random(seed)
                val mutableAll = allNotes.map { it.copy() }.toMutableList()
                mutableAll.shuffle(random)
                
                shuffledList.clear()
                val take50 = mutableAll.take(50)
                shuffledList.addAll(take50)
                totalCount.intValue = shuffledList.size
            }
        }
    }

    fun updateNote(updatedNote: Note) {
        val index = shuffledList.indexOfFirst { it.noteId == updatedNote.noteId }
        if (index != -1) {
            shuffledList[index] = updatedNote
        }
    }

    fun removeTop() {
        if (shuffledList.isNotEmpty()) {
            shuffledList.removeAt(shuffledList.size - 1)
        }
    }
}
