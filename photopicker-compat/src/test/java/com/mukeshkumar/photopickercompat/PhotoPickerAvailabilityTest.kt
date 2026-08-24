package com.mukeshkumar.photopickercompat

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * On API 33+, the Photo Picker ships in the OS itself, so both contracts should
 * route straight to it (`MediaStore.ACTION_PICK_IMAGES`) instead of the
 * `ACTION_OPEN_DOCUMENT` fallback used on older API levels.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhotoPickerAvailabilityTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `single picker uses the real Photo Picker action on API 33+`() {
        val contract = SinglePickerContract(context)
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY)
        assertEquals("android.provider.action.PICK_IMAGES", intent.action)
    }

    @Test
    fun `multi picker uses the real Photo Picker action on API 33+`() {
        val contract = MultiPickerContract(context)
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY to 5)
        assertEquals("android.provider.action.PICK_IMAGES", intent.action)
    }
}
