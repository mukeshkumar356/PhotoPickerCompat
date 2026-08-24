package com.mukeshkumar.photopickercompat

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** See [SinglePickerContractTest] for why this pins an API level below 33. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MultiPickerContractTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val contract = MultiPickerContract(context)

    @Test
    fun `fallback intent allows multiple selection`() {
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY to 5)
        assertTrue(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
    }

    @Test
    fun `fallback intent uses ACTION_OPEN_DOCUMENT`() {
        val intent = contract.createIntent(context, PickerMediaType.IMAGE_ONLY to 5)
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
    }

    @Test
    fun `parseResult reads every uri out of clipData`() {
        val uris = listOf(
            Uri.parse("content://media/external/images/1"),
            Uri.parse("content://media/external/images/2"),
            Uri.parse("content://media/external/images/3")
        )
        val clipData = ClipData.newRawUri("selected", uris[0]).apply {
            addItem(ClipData.Item(uris[1]))
            addItem(ClipData.Item(uris[2]))
        }
        val resultIntent = Intent().apply { this.clipData = clipData }

        val result = contract.parseResult(Activity.RESULT_OK, resultIntent)

        assertEquals(uris, result)
    }

    @Test
    fun `parseResult falls back to single data uri when there is no clipData`() {
        val uri = Uri.parse("content://media/external/images/7")
        val resultIntent = Intent().setData(uri)

        val result = contract.parseResult(Activity.RESULT_OK, resultIntent)

        assertEquals(listOf(uri), result)
    }

    @Test
    fun `parseResult returns an empty list when the user cancels`() {
        val result = contract.parseResult(Activity.RESULT_CANCELED, null)
        assertTrue(result.isEmpty())
    }
}
