package com.ccasey.getitdone

import androidx.lifecycle.Observer

sealed class Resource<T> {

    data class Success<T>(val data: T) : Resource<T>()

    class Loading<T> : Resource<T>()

    data class Error<T>(val error: Throwable) : Resource<T>()
}

interface ResourceView<T> {
    fun showData(data: T)
    fun showLoading(isLoading: Boolean)
    fun showError(error: Throwable)
}

class ResourceViewObserver<T>(private val view: ResourceView<T>) : Observer<Resource<T>> {
    override fun onChanged(resource: Resource<T>?) {
        when(resource) {
            is Resource.Success -> {
                view.showLoading(false)
                view.showData(resource.data)
            }
            is Resource.Error -> {
                view.showLoading(false)
                view.showError(resource.error)
            }
            is Resource.Loading -> {
                view.showLoading(true)
            }
        }
    }
}