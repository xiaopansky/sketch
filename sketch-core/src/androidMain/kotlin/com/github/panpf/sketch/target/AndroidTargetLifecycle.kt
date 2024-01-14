package com.github.panpf.sketch.target

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class AndroidTargetLifecycle(val lifecycle: Lifecycle) : TargetLifecycle() {

    override val currentState: State
        get() = lifecycle.currentState.toTargetLifecycleState()

    init {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    val event1 = event.toTargetLifecycleEvent()
                    observers.forEach { it.onStateChanged(this@AndroidTargetLifecycle, event1) }
                }
            }
        })
    }

    private fun Lifecycle.Event.toTargetLifecycleEvent(): Event {
        return when (this) {
            Lifecycle.Event.ON_CREATE -> Event.ON_CREATE
            Lifecycle.Event.ON_START -> Event.ON_START
            Lifecycle.Event.ON_RESUME -> Event.ON_RESUME
            Lifecycle.Event.ON_PAUSE -> Event.ON_PAUSE
            Lifecycle.Event.ON_STOP -> Event.ON_STOP
            Lifecycle.Event.ON_DESTROY -> Event.ON_DESTROY
            Lifecycle.Event.ON_ANY -> Event.ON_ANY
        }
    }

    private fun Lifecycle.State.toTargetLifecycleState(): State {
        return when (this) {
            Lifecycle.State.INITIALIZED -> State.INITIALIZED
            Lifecycle.State.CREATED -> State.CREATED
            Lifecycle.State.STARTED -> State.STARTED
            Lifecycle.State.RESUMED -> State.RESUMED
            Lifecycle.State.DESTROYED -> State.DESTROYED
        }
    }
}