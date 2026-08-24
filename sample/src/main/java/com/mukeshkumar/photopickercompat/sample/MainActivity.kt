package com.mukeshkumar.photopickercompat.sample

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mukeshkumar.photopickercompat.MultiPhotoPicker
import com.mukeshkumar.photopickercompat.PhotoPicker

/**
 * Demonstrates PhotoPickerCompat end to end. Notice there's no permission
 * request anywhere in this file, and none declared in the manifest either —
 * that's the whole point of the library.
 */
class MainActivity : AppCompatActivity() {

    // Registered in onCreate, per ActivityResultContract's rules.
    private lateinit var singlePicker: PhotoPicker
    private lateinit var multiPicker: MultiPhotoPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val statusText = findViewById<TextView>(R.id.statusText)

        singlePicker = PhotoPicker.register(this, this) { uri ->
            if (uri != null) {
                imagePreview.setImageURI(uri)
                statusText.text = "Picked: $uri"
            } else {
                statusText.text = "Selection cancelled"
            }
        }

        multiPicker = MultiPhotoPicker.register(this, this) { uris ->
            imagePreview.setImageURI(uris.firstOrNull())
            statusText.text = "Picked ${uris.size} item(s)"
        }

        findViewById<Button>(R.id.pickSingleButton).setOnClickListener {
            singlePicker.launch()
        }

        findViewById<Button>(R.id.pickMultipleButton).setOnClickListener {
            multiPicker.launch(maxItems = 5)
        }
    }
}
