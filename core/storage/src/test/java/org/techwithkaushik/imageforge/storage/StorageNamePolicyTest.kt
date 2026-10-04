package org.techwithkaushik.imageforge.storage

import org.junit.Assert.assertEquals
import org.junit.Test

public class StorageNamePolicyTest {
    @Test
    public fun pathSeparatorsAreRemoved() {
        assertEquals("profile_photo.png", StorageNamePolicy.sanitize("../profile_photo.png"))
    }

    @Test
    public fun blankNamesUseSafeFallback() {
        assertEquals("image", StorageNamePolicy.sanitize("   "))
    }
}
