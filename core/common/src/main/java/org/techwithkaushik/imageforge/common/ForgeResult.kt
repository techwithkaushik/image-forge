package org.techwithkaushik.imageforge.common

public sealed interface ForgeResult<out T> {
    public data class Success<T>(val value: T) : ForgeResult<T>
    public data class Failure(val error: ForgeError) : ForgeResult<Nothing>
}

public sealed interface ForgeError {
    public data class InvalidInput(val reason: String) : ForgeError
    public data class UnsupportedFormat(val mimeType: String?) : ForgeError
    public data class ProcessingFailed(val reason: String, val cause: Throwable? = null) : ForgeError
    public data class StorageFailed(val reason: String, val cause: Throwable? = null) : ForgeError
    public data object OutOfMemory : ForgeError
    public data object Cancelled : ForgeError
}