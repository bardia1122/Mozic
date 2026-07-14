package com.example.mozic.core.common.result

/**
 * Success/failure wrapper for one-shot suspend calls that can fail (a network
 * fetch, a DB read). Repositories return this instead of throwing so callers
 * handle both arms explicitly at the boundary. Streaming APIs use `Flow` and
 * their own error handling — this type is only for single results.
 */
sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val cause: Throwable) : Result<Nothing>
}

/** Maps the success value, leaving an [Result.Error] untouched. */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

/** The value on success, or `null` on error. */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}
