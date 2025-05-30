package com.example.qwizz.viewmodel.makeqwizz

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qwizz.data.control.QwizzControl
import com.example.qwizz.data.model.QuizQuestion
import com.example.qwizz.data.model.Qwizzz
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MakeQwizzzViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val id = auth.currentUser?.uid
    private val qwizzControl = QwizzControl()

    private val _qwizzzState = MutableStateFlow<QwizzzState>(QwizzzState.Initial)
    val qwizzzState = _qwizzzState.asStateFlow()

    private val _qwizzz = MutableStateFlow<Qwizzz>( Qwizzz())
    val qwizzz = _qwizzz.asStateFlow()


    val isLoading = mutableStateOf(false)


     fun saveQwizzzToDB(topic : String, title: String, totalDetik: Int, qwizQuestion: List<QuizQuestion>, onResult: (success: Boolean, message: String?) -> Unit) {
         if (id == null) {
             onResult(false, "User not authenticated")
             return
         }
         viewModelScope.launch {

             isLoading.value = true
             _qwizzzState.value = QwizzzState.Loading

            try {

                val result = qwizzControl.addQwizzz(id.toString(), topic, title, totalDetik, qwizQuestion)
                if (result) {
                    Log.d(ContentValues.TAG, "Qwizzz saved successfully")
                    _qwizzzState.value = QwizzzState.QwizzzSaved
                    onResult(true, "Qwizzz saved successfully")
                } else{
                    Log.e(ContentValues.TAG, "Qwizzz failed to save")
                    _qwizzzState.value = QwizzzState.QwizzzNotSaved
                    onResult(false, "Qwizzz failed to save")
                }
            }catch ( e: Exception){
                Log.e(ContentValues.TAG, "Qwizzz failed to save: ${e.message}")
                _qwizzzState.value = QwizzzState.Error(e.message ?: "Unknown error")
                onResult(false, e.message)
            }finally {
                isLoading.value = false
            }

         }
    }

    fun checkTitle(title: String, topic: String, onResult: (success: Boolean, message: String?) -> Unit) {
        if (id == null) {
            onResult(false, "User not authenticated")
            return
        }
        viewModelScope.launch {
            try {
                val title = qwizzControl.getTitleQwizzz(title, topic)
                if (title.isNotEmpty()) {
                    onResult(false, "Anda telah membuat qwizzz dengan judul: $title")
                } else {
                    onResult(true, "Silahkan buat qwizzz")
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error checking title: ${e.message}")
                onResult(false, e.message)
            }
        }
    }

}

sealed class QwizzzState {
    object Initial : QwizzzState()
    object Loading : QwizzzState()
    object QwizzzSaved : QwizzzState()
    object QwizzzNotSaved : QwizzzState()
    data class Error(val message: String) : QwizzzState()

}