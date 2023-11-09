package com.redline.anistalker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ExecutionFlow(
    private val concurrentCount: Int,
    private val scope: CoroutineScope,
) {
    private val queue = ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>()
    private val currentCount = AtomicInteger(0)

    fun execute(callback: suspend CoroutineScope.() -> Unit) {
        queue.add(callback)
        operateTasks()
    }

    fun isEmpty(): Boolean = queue.size == 0

    private fun operateTasks() {
        if (currentCount.get() < concurrentCount) queue.poll()?.let {
            currentCount.incrementAndGet()

            scope.launch {
                it()
            }.invokeOnCompletion {
                currentCount.decrementAndGet()
                operateTasks()
            }
        }
    }
}