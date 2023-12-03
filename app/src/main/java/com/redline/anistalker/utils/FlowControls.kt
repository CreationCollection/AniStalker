package com.redline.anistalker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ExecutionFlow(
    private val concurrentCount: Int,
    private val scope: CoroutineScope,
) {
    private val queue = ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>()
    private val currentCount = AtomicInteger(0)
    private var completionJob = Job()

    fun execute(forced: Boolean = false, callback: suspend CoroutineScope.() -> Unit) {
        if (forced) runTask(callback)
        else {
            queue.add(callback)
            operateTasks()
        }
    }

    fun isEmpty(): Boolean = queue.size == 0 && currentCount.get() <= 0

    fun await() {
        runBlocking { completionJob.join() }
        completionJob = Job()
    }

    fun reset() {
        queue.clear()
        currentCount.set(0)
    }

    private fun operateTasks() {
        if (currentCount.get() < concurrentCount)
            queue.poll()?.let { runTask(it, true) }
    }

    private fun runTask(task: suspend CoroutineScope.() -> Unit, forward: Boolean = false) {
        currentCount.incrementAndGet()

        scope.launch {
            try { task() } catch (err: Exception) { err.printStackTrace() }
        }.invokeOnCompletion {
            currentCount.decrementAndGet()
            if (forward) operateTasks()

            if (isEmpty()) {
                completionJob.complete()
            }
        }
    }
}

class KeyExecutionFlow<T>(
    private val concurrentCount: Int,
    private val scope: CoroutineScope
) {
    private val queue = ConcurrentHashMap<T, ConcurrentLinkedQueue<suspend CoroutineScope.(key: T) -> Unit>>()
    private val currentCount = ConcurrentHashMap<T, AtomicInteger>()

    fun execute(key: T, block: suspend CoroutineScope.(T) -> Unit) {
        queue.getOrPut(key) { ConcurrentLinkedQueue() }.apply {
            add(block)
        }
        currentCount.getOrPut(key) { AtomicInteger(0) }
        operateTask(key)
    }

    fun isEmpty(key: T): Boolean {
        val count = currentCount[key]
        val pending = queue[key]?.size

        if (count == null || pending == null) return false
        return count.get() <= 0 && pending == 0
    }

    private fun operateTask(key: T) {
        if (currentCount[key]!!.get() < concurrentCount) queue[key]!!.poll()?.let {
            currentCount[key]!!.incrementAndGet()

            scope.launch {
                try { it(key) } catch (err: Exception) { err.printStackTrace() }
            }.invokeOnCompletion {
                currentCount[key]!!.decrementAndGet()

                queue[key]?.let {
                    if (it.size == 0) {
                        queue.remove(key)
                        currentCount.remove(key)
                    }
                    else operateTask(key)
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