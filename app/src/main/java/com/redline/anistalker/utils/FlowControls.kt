package com.redline.anistalker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ExecutionFlow(
    private val concurrentCount: Int,
    private val scope: CoroutineScope,
) {
    private val queue = ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>()
    private val currentCount = AtomicInteger(0)
    private val completionJob = Job()

    fun execute(callback: suspend CoroutineScope.() -> Unit) {
        queue.add(callback)
        operateTasks()
    }

    fun isEmpty(): Boolean = queue.size == 0 && currentCount.get() <= 0

    fun await() {
        runBlocking { completionJob.join() }
    }

    private fun operateTasks() {
        if (currentCount.get() < concurrentCount) queue.poll()?.let {
            currentCount.incrementAndGet()

            scope.launch {
                try { it() } catch (err: Exception) { err.printStackTrace() }
            }.invokeOnCompletion {
                currentCount.decrementAndGet()
                operateTasks()

                if (isEmpty()) {
                    completionJob.complete()
                }
            }
        }
    }
}


fun<A, B> runWith(a: A, b: B, use: (a: A, b: B) -> Unit) {
    use(a, b)
}

fun<A, B, C> runWith(a: A, b: B, c: C, use: (a: A, b: B, c: C) -> Unit) {
    use(a, b, c)
}

fun<A, B, C, D> runWith(a: A, b: B, c: C, d: D, use: (a: A, b: B, c: C, d: D) -> Unit) {
    use(a, b, c, d)
}

fun<A, B, C, D, E> runWith(a: A, b: B, c: C, d: D, e: E, use: (a: A, b: B, c: C, d: D, e: E) -> Unit) {
    use(a, b, c, d, e)
}

fun<G> G.runWith(vararg values: Any, block: G.(values: Array<out Any>) -> Unit) {
    block(values)
}