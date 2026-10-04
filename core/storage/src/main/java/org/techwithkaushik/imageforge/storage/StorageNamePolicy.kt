package org.techwithkaushik.imageforge.storage

public object StorageNamePolicy {
    public fun sanitize(displayName: String): String =
        displayName
            .replace('/', '_')
            .replace('\\', '_')
            .trim()
            .ifBlank { "image" }
}
