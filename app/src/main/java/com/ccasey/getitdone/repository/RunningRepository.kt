package com.ccasey.getitdone.repository

import com.ccasey.getitdone.Resource
import com.ccasey.getitdone.model.Run
import com.ccasey.getitdone.storage.PreferenceHelper

class RunningRepository(val preferenceHelper: PreferenceHelper) {

    suspend fun getCurrentRunMetrics(): Resource<Run> {
        return loadApiResource { preferenceHelper.getCurrentRunMetrics() }
    }

    fun setCurrentRunMetrics(run: Run) {
        preferenceHelper.setCurrentRunMetrics(run)
    }

    suspend fun getDurationLeft(): Resource<Long> {
        return loadApiResource { preferenceHelper.getDurationLeft() }
    }

    fun setDurationLeft(left: Long) {
        preferenceHelper.setDurationLeft(left)
    }

    suspend fun <T> loadApiResource(loader: suspend() -> T): Resource<T> {
        return try {
            Resource.Success(loader())
        } catch (error: Exception) {
            error.printStackTrace()
            return Resource.Error(error)
        }
    }
}