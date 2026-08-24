package com.mukeshkumar.photopickercompat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Picks a single item. Uses the real Photo Picker when available; otherwise falls
 * back to `ACTION_OPEN_DOCUMENT`, which — like the Photo Picker — needs no runtime
 * permission and is scoped-storage safe.
 */
internal class SinglePickerContract(
    context: Context
) : ActivityResultContract<PickerMediaType, Uri?>() {

    private val photoPickerAvailable =
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
    private val delegate = ActivityResultContracts.PickVisualMedia()

    override fun createIntent(context: Context, input: PickerMediaType): Intent {
        return if (photoPickerAvailable) {
            delegate.createIntent(
                context,
                PickVisualMediaRequest.Builder().setMediaType(input.toVisualMediaType()).build()
            )
        } else {
            fallbackIntent(input, allowMultiple = false)
        }
    }

    override fun getSynchronousResult(
        context: Context,
        input: PickerMediaType
    ): SynchronousResult<Uri?>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (photoPickerAvailable) {
            delegate.parseResult(resultCode, intent)
        } else {
            if (resultCode == Activity.RESULT_OK) intent?.data else null
        }
    }
}

/**
 * Picks multiple items. [maxItems] is only honored by the real Photo Picker — the
 * OS enforces that limit itself; the `ACTION_OPEN_DOCUMENT` fallback has no such
 * concept, so on older devices the user can select as many as they like.
 */
internal class MultiPickerContract(
    context: Context
) : ActivityResultContract<Pair<PickerMediaType, Int>, List<Uri>>() {

    private val photoPickerAvailable =
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
    private var activeDelegate: ActivityResultContracts.PickMultipleVisualMedia? = null

    override fun createIntent(context: Context, input: Pair<PickerMediaType, Int>): Intent {
        val (mediaType, maxItems) = input
        return if (photoPickerAvailable) {
            val delegate = ActivityResultContracts.PickMultipleVisualMedia(maxItems)
            activeDelegate = delegate
            delegate.createIntent(
                context,
                PickVisualMediaRequest.Builder().setMediaType(mediaType.toVisualMediaType()).build()
            )
        } else {
            activeDelegate = null
            fallbackIntent(mediaType, allowMultiple = true)
        }
    }

    override fun getSynchronousResult(
        context: Context,
        input: Pair<PickerMediaType, Int>
    ): SynchronousResult<List<Uri>>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        activeDelegate?.let { return it.parseResult(resultCode, intent) }
        if (resultCode != Activity.RESULT_OK || intent == null) return emptyList()
        val clipData = intent.clipData
        return if (clipData != null) {
            (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
        } else {
            intent.data?.let { listOf(it) } ?: emptyList()
        }
    }
}

private fun fallbackIntent(mediaType: PickerMediaType, allowMultiple: Boolean): Intent =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = mediaType.mimeType
        if (allowMultiple) putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }
