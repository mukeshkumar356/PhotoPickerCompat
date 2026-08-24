package com.mukeshkumar.photopickercompat

import org.junit.Assert.assertEquals
import org.junit.Test

class PickerMediaTypeTest {

    @Test
    fun `image only maps to image mime type`() {
        assertEquals("image/*", PickerMediaType.IMAGE_ONLY.mimeType)
    }

    @Test
    fun `video only maps to video mime type`() {
        assertEquals("video/*", PickerMediaType.VIDEO_ONLY.mimeType)
    }

    @Test
    fun `image and video maps to wildcard mime type`() {
        assertEquals("*/*", PickerMediaType.IMAGE_AND_VIDEO.mimeType)
    }
}
