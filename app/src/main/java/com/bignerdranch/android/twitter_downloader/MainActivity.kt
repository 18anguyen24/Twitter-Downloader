package com.bignerdranch.android.twitter_downloader

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.twitter_downloader.api.getTweetJSONByID
import kotlinx.coroutines.runBlocking
import android.util.Log
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL


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
            //get user input
            string = editText.text.toString()
            textView.text = string

            //parsing string and grab id of tweet
            val parse_question = string.split("?").toTypedArray()
            val parse_slash = parse_question[0].split("/").toTypedArray()
            val twitter_id = parse_slash.last()

            //feed id to function
            test(twitter_id)
            val request = DownloadManager.Request(Uri.parse("url string, will add later after we get return result from json"))
                .setTitle("File")
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            //dm.enqueue(request)

            Toast.makeText(this@MainActivity, "Downloading...", Toast.LENGTH_SHORT).show()
        }

    }
    fun downloadFile(url: URL, fileName: String) {
        url.openStream().use { inp ->
            BufferedInputStream(inp).use { bis ->
                FileOutputStream(fileName).use { fos ->
                    val data = ByteArray(1024)
                    var count: Int
                    while (bis.read(data, 0, 1024).also { count = it } != -1) {
                        fos.write(data, 0, count)
                    }
                }
            }
        }
    }

    fun test(id: String) = runBlocking() {
        getTweetJSONByID(id)

    }

}