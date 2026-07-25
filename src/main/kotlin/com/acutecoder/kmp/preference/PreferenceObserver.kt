package com.acutecoder.kmp.preference

fun interface Observer {
    fun onPreferenceChange()
}

object PreferenceObserver {

    private val observers = mutableListOf<Observer>()

    fun observe(observer: Observer) {
        observers.add(observer)
    }

    fun remove(observer: Observer) {
        observers.remove(observer)
    }

    fun emit() {
        observers.forEach(Observer::onPreferenceChange)
    }

}
