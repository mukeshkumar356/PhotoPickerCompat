package com.mukeshkumar.photopickercompat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable]
 * always reports available on API 33+ (the Photo Picker ships in the OS itself
 * from Android 13 on), so these tests pin an API level below that — the only
 * way to deterministically exercise the fallback path without a real
 * pre-Android-13 device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SinglePickerContractTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val contract = SinglePickerContract(context)

    @Test
    fun `fallback intent uses ACTION_OPEN_DOCUMENT`() {
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY)
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
    }

    @Test
    fun `fallback intent is openable and typed for images`() {
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY)
        assertTrue(intent.categories?.contains(Intent.CATEGORY_OPENABLE) == true)
        assertEquals("image/*", intent.type)
    }

    @Test
    fun `fallback intent respects video only media type`() {
        val intent = contract.createIntent(context, PickerMediaType.VIDEO_ONLY)
        assertEquals("video/*", intent.type)
    }

    @Test
    fun `fallback intent is single-select, no EXTRA_ALLOW_MULTIPLE`() {
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY)
        assertTrue(!intent.hasExtra(Intent.EXTRA_ALLOW_MULTIPLE))
    }

    @Test
    fun `parseResult returns the picked uri on RESULT_OK`() {
        val uri = Uri.parse("content://media/external/images/42")
        val resultIntent = Intent().setData(uri)
        val result = contract.parseResult(Activity.RESULT_OK, resultIntent)
        assertEquals(uri, result)
    }

    @Test
    fun `parseResult returns null when the user cancels`() {
        val result = contract.parseResult(Activity.RESULT_CANCELED, null)
        assertNull(result)
    }
}
