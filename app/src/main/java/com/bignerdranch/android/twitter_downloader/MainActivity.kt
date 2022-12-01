package com.bignerdranch.android.twitter_downloader

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import android.util.Log
import com.bignerdranch.android.twitter_downloader.api.Tweet.Companion.getTweetJSONByID
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import twitter4j.MediaKey

private const val TAG = "MainActivity"

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

        //TESTING TWEETS ONLY
        //tweet with video
        //test("1598154884003438593")
        //tweet with only text
        //test("1598347132385239041")
        //tweet with only images
        //test("1598345429363613699")

        button.setOnClickListener {
            //get user input
            string = editText.text.toString()
            textView.text = string

            //parsing string and grab id of tweet
            val parse_question = string.split("?").toTypedArray()
            val parse_slash = parse_question[0].split("/").toTypedArray()
            val twitter_id = parse_slash.last()

            //feed id to function
            //now returns url of video, when given tweet ID, will be null if tweet has no video
            val videoURL = test(twitter_id)
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
        val tweetMediaMap = getTweetJSONByID(id).mediaMap
        //val testKeyToMake = MediaKey("3_1598345418198470657")
        //val hasKey = testVal.containsKey(testKeyToMake)
        Log.d(TAG, tweetMediaMap.toString())

        //key value for media that is the video, could be missing
        var keyForVideo:MediaKey?=null

        if(tweetMediaMap.isNotEmpty()) {
            for ((key,value) in tweetMediaMap) {
                if(value.type.toString() == "Video"){
                    keyForVideo = key
                }
            }
            if (keyForVideo != null) {
                val value = tweetMediaMap.get(keyForVideo)
                val videoInfo = value?.asVideo
                val variants = videoInfo?.variants
                //WRITE FUNCTION CALL OR SOMETHING THAT GETS VIDEO BASED ON QUALITY?
                val firstVariant = variants?.get(variants.size-2)
                val variantUrl = firstVariant?.url
                Log.d(TAG, variantUrl!!)
                return@runBlocking variantUrl
            }
            else{
                //TWEET IS MISSING VIDEO
                return@runBlocking null
            }
        }
        else{
            //TWEET IS MISSING MEDIA
            return@runBlocking null
        }
    }

}