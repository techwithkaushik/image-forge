package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Test

public class ImageOperationTest {
    @Test(expected = IllegalArgumentException::class)
    public fun resizeRejectsZeroWidth() {
        ImageOperation.Resize(0, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun cropRejectsZeroHeight() {
        ImageOperation.Crop(0, 0, 100, 0)
    }
}
