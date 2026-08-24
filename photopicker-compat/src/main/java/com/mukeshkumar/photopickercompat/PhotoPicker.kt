package com.mukeshkumar.photopickercompat

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher

/**
 * A drop-in replacement for `Intent.ACTION_GET_CONTENT` + the
 * `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` permissions that:
 *
 *  1. Uses the modern Android **Photo Picker**
 *     ([androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia])
 *     whenever it's available on the device — no runtime permission needed, and
 *     it satisfies Google Play's *Photo and Video Permissions* policy for apps
 *     that only need occasional, user-initiated media access.
 *  2. Automatically falls back to `ACTION_OPEN_DOCUMENT` (also permission-free
 *     and scoped-storage-safe) on devices where the Photo Picker isn't
 *     available, instead of silently failing — or worse, falling back to the
 *     *old* broad-permission approach that gets apps rejected in review.
 *
 * ### Why this exists
 * This library exists because I hit exactly this problem shipping
 * [#HashKit](https://github.com/mukeshkumar356/hashkit-android) — Google Play
 * rejected the app for using `ACTION_GET_CONTENT` + `READ_MEDIA_IMAGES` for a
 * feature that only ever needed one photo at a time. The real fix was small,
 * but every project hits the same wall, so this is that fix, generalized.
 *
 * ### Usage
 * ```kotlin
 * class MyActivity : ComponentActivity() {
 *     private lateinit var photoPicker: PhotoPicker
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // Register unconditionally, before STARTED — same rule as any
 *         // ActivityResultContract.
 *         photoPicker = PhotoPicker.register(this, this) { uri ->
 *             uri?.let { showImage(it) }
 *         }
 *     }
 *
 *     private fun onPickImageClicked() {
 *         photoPicker.launch()
 *     }
 * }
 * ```
 */
class PhotoPicker private constructor(
    private val launcher: ActivityResultLauncher<PickerMediaType>
) {
    /** Opens the picker. [mediaType] defaults to images only. */
    fun launch(mediaType: PickerMediaType = PickerMediaType.IMAGE_ONLY) {
        launcher.launch(mediaType)
    }

    companion object {
        /**
         * Registers a single-item picker.
         *
         * Must be called unconditionally on every `onCreate`/`onCreateView` —
         * that's a hard requirement of [ActivityResultCaller] itself, not
         * specific to this library.
         */
        fun register(
            caller: ActivityResultCaller,
            context: Context,
            onResult: (Uri?) -> Unit
        ): PhotoPicker {
            val launcher = caller.registerForActivityResult(SinglePickerContract(context), onResult)
            return PhotoPicker(launcher)
        }
    }
}

/**
 * Same idea as [PhotoPicker], for multi-select. See [PhotoPicker] for the full
 * rationale and the registration rules.
 */
class MultiPhotoPicker private constructor(
    private val launcher: ActivityResultLauncher<Pair<PickerMediaType, Int>>
) {
    /**
     * Opens the picker. [maxItems] is only enforced by the real Photo Picker —
     * see the class doc on [MultiPickerContract] for the fallback behavior.
     */
    fun launch(mediaType: PickerMediaType = PickerMediaType.IMAGE_ONLY, maxItems: Int = 5) {
        launcher.launch(mediaType to maxItems)
    }

    companion object {
        fun register(
            caller: ActivityResultCaller,
            context: Context,
            onResult: (List<Uri>) -> Unit
        ): MultiPhotoPicker {
            val launcher = caller.registerForActivityResult(MultiPickerContract(context), onResult)
            return MultiPhotoPicker(launcher)
        }
    }
}
