package com.ccasey.getitdone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ccasey.getitdone.Resource
import com.ccasey.getitdone.model.Run
import com.ccasey.getitdone.repository.RunningRepository
import com.ccasey.getitdone.storage.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RunningViewModel(application: Application) : AndroidViewModel(application) {
    val repo: RunningRepository = RunningRepository(PreferenceHelper(application.applicationContext))

    val runMetricsLiveData = MutableLiveData<Resource<Run>>()
    val durationLeftLiveData = MutableLiveData<Resource<Long>>()

    fun getCurrentRunMetrics() {
        runMetricsLiveData.loadResource { repo.getCurrentRunMetrics() }
    }

    fun setCurrentRunMetrics(run: Run) {
        repo.setCurrentRunMetrics(run)
    }

    fun getDurationLeft() {
        durationLeftLiveData.loadResource { repo.getDurationLeft() }
    }

    fun setDurationLeft(left: Long) {
        repo.setDurationLeft(left)
    }

    fun <T> MutableLiveData<Resource<T>>.loadResource(valueLoader: suspend () -> Resource<T>) {
        this.loadResource(valueLoader, { it })
    }

    fun <T, R> MutableLiveData<R>.loadResource(valueLoader: suspend () -> Resource<T>, mapper: (Resource<T>) -> R) {
        val resourceLiveData = this
        CoroutineScope(Job() + Dispatchers.Main).launch {
            resourceLiveData.value = mapper(Resource.Loading())
            try {
                resourceLiveData.value = mapper(valueLoader())
            } catch (error: Exception) {
                resourceLiveData.value = mapper(Resource.Error(error))
            }
        }
    }
}