package me.ash.reader.infrastructure.net

import kotlinx.coroutines.*

data class RetryConfig(
    val attempts: Int = 5,
    val timeoutPerAttempt: Long = 5000L,
    val initialDelay: Long = 1000L,
    val maxDelay: Long = 20000L,
    val delayFactor: Double = 2.0,
    val onRetry: (() -> Unit)? = null,
    val shouldRetry: (Throwable) -> Boolean = { it !is CancellationException },
)

class TimeoutException(val timeout: Long) : Exception("Task attempt timed out after $timeout ms")

sealed class RetryableTaskResult<out R> {
    data class Success<out T>(val value: T) : RetryableTaskResult<T>()
    data class Failure(
        val finalException: Throwable? = null, val attemptExceptions: List<Throwable> = emptyList()
    ) : RetryableTaskResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrThrow(): R {
        return when (this) {
            is Success -> value
            is Failure -> throw finalException ?: IllegalStateException("No exception was thrown")
        }
    }

    fun getOrNull(): R? {
        return when (this) {
            is Success -> value
            is Failure -> null
        }
    }
}

suspend fun <R> withRetries(
    config: RetryConfig = RetryConfig(), block: suspend () -> R
): RetryableTaskResult<R> {
    val attemptExceptions = mutableListOf<Throwable>()
    with(config) {
        var currentDelay = initialDelay

        for (attempt in 1..attempts) {
            try {
                val result = withTimeoutOrNull(timeoutPerAttempt) {
                    block()
                }

                if (result != null) {
                    return RetryableTaskResult.Success(result)
                } else {
                    throw TimeoutException(timeoutPerAttempt)
                }
            } catch (th: Throwable) {
                attemptExceptions.add(th)

                if (attempt < attempts) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * delayFactor).toLong().coerceAtMost(maxDelay)
                }

                if (!shouldRetry(th) || attempt == attempts) {
                    return RetryableTaskResult.Failure(
                        finalException = th, attemptExceptions = attemptExceptions
                    )
                }

                onRetry?.invoke()
            }
        }
        return RetryableTaskResult.Failure(
            finalException = attemptExceptions.lastOrNull(), attemptExceptions = attemptExceptions
        )
    }
}

suspend fun <R> withRetriesOrNull(config: RetryConfig, block: suspend () -> R?): R? =
    withRetries(config, block).getOrNull()