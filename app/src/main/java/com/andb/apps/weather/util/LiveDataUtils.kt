package com.andb.apps.weather.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**MediatorLiveData of List<T> with better sync to backing list and better modification methods**/
open class ListLiveData<T>(initialList: List<T> = emptyList()) : MediatorLiveData<List<T>>(),
    List<T> {

    private val backingList: MutableList<T> = initialList.toMutableList()

    fun add(item: T) {
        backingList.add(item)
        postValue(backingList)
    }

    fun add(item: T, index: Int = backingList.size) {
        backingList.add(index, item)
        postValue(backingList)
    }

    fun addAll(items: Collection<T>) {
        backingList.addAll(items)
        postValue(backingList)
    }

    fun remove(item: T) {
        backingList.remove(item)
        postValue(backingList)
    }

    fun removeAt(index: Int) {
        backingList.removeAt(index)
        postValue(backingList)
    }

    fun drop(by: Int) {
        backingList.dropLast(by)
        postValue(backingList)
    }

    fun clear() {
        backingList.clear()
        postValue(backingList)
    }

    fun last(): T {
        return backingList.last()
    }


    fun lastOrNull(): T? {
        return backingList.lastOrNull()
    }

    override fun postValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.postValue(backingList)
    }

    override fun setValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.setValue(backingList)
    }

    override fun getValue(): List<T> {
        return backingList
    }

    override val size: Int
        get() = backingList.size

    override fun contains(element: T): Boolean = backingList.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = backingList.containsAll(elements)

    override fun get(index: Int): T = backingList[index]

    override fun indexOf(element: T): Int = backingList.indexOf(element)

    override fun isEmpty(): Boolean = backingList.isEmpty()

    override fun iterator(): Iterator<T> = backingList.iterator()

    override fun lastIndexOf(element: T): Int = backingList.lastIndexOf(element)

    override fun listIterator(): ListIterator<T> = backingList.listIterator()

    override fun listIterator(index: Int): ListIterator<T> = backingList.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> =
        backingList.subList(fromIndex, toIndex)

}

class InitialLiveData<T>(private val initialValue: T) : MediatorLiveData<T>() {
    init {
        this.value = initialValue
    }

    override fun getValue(): T {
        return super.getValue() ?: initialValue
    }
}

@ExperimentalCoroutinesApi
fun <T> LiveData<T>.asFlow() = channelFlow<T> {
    val initial = this@asFlow.value
    if (initial != null) {
        offer(initial)
    }
    val observer = Observer<T> { t -> offer(t) }
    observeForever(observer)
    awaitClose {
        removeObserver(observer)
    }
}

@ExperimentalCoroutinesApi
fun <T> Flow<T>.toLiveData(scope: CoroutineScope) = object : MediatorLiveData<T>() {
    override fun onActive() {
        super.onActive()
        scope.launch {
            this@toLiveData.collect { postValue(it) }
        }
    }
}

fun <T, R> LiveData<T>.mapAsync(transform: suspend (T) -> R): LiveData<R> {
    val mld = MediatorLiveData<R>()
    mld.addSource(this) { item ->
        newIoThread {
            mld.postValue(transform.invoke(item))
        }
    }
    return mld
}

fun <T> LiveData<T?>.notNull(): LiveData<T> {
    return object : MediatorLiveData<T>() {
        init {
            addSource(this@notNull) {
                if (it != null) {
                    this.postValue(it)
                }
            }
        }
    }
}

class LoadingLiveData<T>(initalValue: Boolean = false) : MediatorLiveData<T>() {
    val loading = InitialLiveData(initalValue)
    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        super.addSource(source) {
            Log.d("loadingLiveData", "starting load")
            loading.postValue(true)
            onChanged.onChanged(it)
            Log.d("loadingLiveData", "end load")
            loading.postValue(false)
        }
    }
}

fun <T> LiveData<T>.withLoading(): LoadingLiveData<T> {
    val lld = LoadingLiveData<T>()
    lld.addSource(this) {}
    return lld
}