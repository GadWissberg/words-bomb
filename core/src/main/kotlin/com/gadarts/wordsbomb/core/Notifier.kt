package com.gadarts.wordsbomb.core

interface Notifier<T> {
    val subscribers: HashSet<T>


    fun subscribeForEvents(subscriber: T) {
        subscribers.add(subscriber)
    }


}
