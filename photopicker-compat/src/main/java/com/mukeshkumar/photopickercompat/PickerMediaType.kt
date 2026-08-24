package com.mukeshkumar.photopickercompat

import androidx.activity.result.contract.ActivityResultContracts

/**
 * The kind of media the user is allowed to pick.
 */
enum class PickerMediaType(internal val mimeType: String) {
    IMAGE_ONLY("image/*"),
    VIDEO_ONLY("video/*"),
    IMAGE_AND_VIDEO("*/*");

    internal fun toVisualMediaType(): ActivityResultContracts.PickVisualMedia.VisualMediaType =
        when (this) {
            IMAGE_ONLY -> ActivityResultContracts.PickVisualMedia.ImageOnly
            VIDEO_ONLY -> ActivityResultContracts.PickVisualMedia.VideoOnly
            IMAGE_AND_VIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }
}
