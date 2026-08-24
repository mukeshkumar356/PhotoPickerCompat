# PhotoPickerCompat

[![CI](https://github.com/mukeshkumar356/PhotoPickerCompat/actions/workflows/android-ci.yml/badge.svg)](https://github.com/mukeshkumar356/PhotoPickerCompat/actions/workflows/android-ci.yml)
[![JitPack](https://jitpack.io/v/mukeshkumar356/PhotoPickerCompat.svg)](https://jitpack.io/#mukeshkumar356/PhotoPickerCompat)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![minSdk 21](https://img.shields.io/badge/minSdk-21-brightgreen.svg)]()

A tiny Kotlin wrapper around Android's modern **Photo Picker** — with an automatic, permission-free fallback for devices where it isn't available. No `READ_MEDIA_IMAGES`, no `READ_EXTERNAL_STORAGE`, on any API level.

## Why this exists

I hit this exact problem shipping **[#HashKit](https://github.com/mukeshkumar356/hashkit-android)**: Google Play rejected the app for using `ACTION_GET_CONTENT` + `READ_MEDIA_IMAGES` for a feature (an on-device AI hashtag generator) that only ever needed **one** user-picked photo at a time. Google's own fix is the Photo Picker — but most sample code stops at "call `PickVisualMedia` on API 33+" and quietly ignores what happens on the ~40% of active devices still below that.

`isPhotoPickerAvailable()` already tells you whether the picker is there (native on API 33+, or backported to API 30+ via a Google Play system update). What's missing is a good answer for *else*. `PhotoPickerCompat` is that answer: it checks availability for you and falls back to `ACTION_OPEN_DOCUMENT` — which is just as permission-free and scoped-storage-safe — instead of leaving you to reach for the old broad-permission approach that gets apps rejected in review.

## Install

Add JitPack to your root `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:

```groovy
dependencies {
    implementation 'com.github.mukeshkumar356:PhotoPickerCompat:1.0.0'
}
```

## Usage

```kotlin
class MyActivity : ComponentActivity() {

    // Register unconditionally in onCreate — same rule as any
    // ActivityResultContract.
    private lateinit var photoPicker: PhotoPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoPicker = PhotoPicker.register(this, this) { uri ->
            uri?.let { imageView.setImageURI(it) }
        }
    }

    private fun onPickImageClicked() {
        photoPicker.launch()
    }
}
```

Multiple images, or video:

```kotlin
private lateinit var multiPicker: MultiPhotoPicker

multiPicker = MultiPhotoPicker.register(this, this) { uris ->
    uris.forEach { addToGallery(it) }
}

// later
multiPicker.launch(mediaType = PickerMediaType.IMAGE_AND_VIDEO, maxItems = 10)
```

No manifest entry, no `registerForActivityResult` boilerplate, no permission check — that's the whole API surface.

## How it decides

| Device support                                   | What actually opens              |
|---------------------------------------------------|-----------------------------------|
| API 33+ (native), or API 30–32 with the Play system update | The real Photo Picker (`ActivityResultContracts.PickVisualMedia`) |
| Everything else                                    | `ACTION_OPEN_DOCUMENT` — same zero-permission, scoped-storage-safe contract, just without the Picker's dedicated UI |

Both branches return a plain `content://` `Uri` (or `List<Uri>` for multi-select), so your code never needs to know which path was taken.

## Sample app

The [`sample`](sample) module is a minimal, real Activity — two buttons, an `ImageView`, zero permissions in its manifest — that exercises both `PhotoPicker` and `MultiPhotoPicker` end to end. Clone the repo and run `:sample` to try it.

## Testing

The contract logic is covered by Robolectric-backed unit tests (`:photopicker-compat:testDebugUnitTest`) that pin specific API levels to exercise **both** branches deterministically — API 28 for the `ACTION_OPEN_DOCUMENT` fallback, API 33 for the real Photo Picker path — rather than relying on whatever the host machine happens to report.

## License

MIT — see [LICENSE](LICENSE).
