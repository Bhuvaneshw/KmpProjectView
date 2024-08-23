package com.acutecoder.kmp.projectview.preference

typealias Observer = () -> Unit

object PreferenceObserver {

    private val observers = mutableListOf<Observer>()

    fun observe(observer: Observer) {
        observers.add(observer)
    }

    fun emit() {
        observers.forEach { observer ->
            observer()
        }
    }

}
