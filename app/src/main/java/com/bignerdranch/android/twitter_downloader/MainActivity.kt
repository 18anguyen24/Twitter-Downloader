package com.bignerdranch.android.twitter_downloader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.twitter_downloader.api.getTweetJSONByID
import kotlinx.coroutines.runBlocking


private const val TAG = "Main Activity"

class MainActivity : AppCompatActivity() {

    lateinit var button: Button
    lateinit var editText: EditText
    lateinit var string: String //use this to store the data of the EditText
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        editText = findViewById(R.id.textInputEditText)
        textView = findViewById(R.id.textView)

        button.setOnClickListener {
            string = editText.text.toString()
            textView.text = string
            Toast.makeText(this@MainActivity, "Downloading...", Toast.LENGTH_SHORT).show()
        }

        test()
    }

    fun test() = runBlocking() {
        getTweetJSONByID("1591434056323203072")

    }

}