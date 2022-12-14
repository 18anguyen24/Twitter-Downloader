package com.bignerdranch.android.twitter_downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.twitter_downloader.api.TwitterAPI
import com.bignerdranch.android.twitter_downloader.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.runBlocking
import twitter4j.MediaKey
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var dLButton: Button
    private lateinit var pasteButton: Button
    private lateinit var editText: EditText
    private lateinit var linkInputLayout: TextInputLayout
    private lateinit var string: String //use this to store the data of the EditText
    private lateinit var mySpinner: Spinner
    private lateinit var binding: ActivityMainBinding

    private val twitterAPI = TwitterAPI()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dLButton = findViewById(R.id.button)
        linkInputLayout = findViewById(R.id.linkContainer)
        editText = findViewById(R.id.textInputEditText)
        pasteButton = findViewById(R.id.button3)
        mySpinner = findViewById(R.id.quality_spinner)

        linkInputLayout.isHintEnabled = false

        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData = ""

        val qualities = resources.getStringArray(R.array.video_quality_options)
        val spinnerAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, qualities) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if(position == 0) {
                    view.setTextColor(Color.GRAY)
                } else {
                    //here it is possible to define color for other items by
                    //view.setTextColor(Color.RED)
                }
                return view
            }
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = spinnerAdapter

        //TESTING TWEETS ONLY
        //tweet with video
        //test("1598154884003438593")
        //tweet with only text
        //test("1598347132385239041")
        //tweet with only images
        //test("1598345429363613699")

        mySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = parent!!.getItemAtPosition(position).toString()
                if(value == qualities[0]){
                    (view as TextView).setTextColor(Color.GRAY)
                }
            }

        }

        //Listener to grab first data off of clipboard stack(if its valid plaintext) and place it in EditText field
        pasteButton.setOnClickListener {
            // If it does contain data, decide if you can handle the data.
            if (!clipboard.hasPrimaryClip()) {

            } else if (!clipboard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                // since the clipboard has data but it is not plain text

            } else {

                //since the clipboard contains plain text.
                val item = clipboard.primaryClip!!.getItemAt(0)

                // Gets the clipboard as text.
                pasteData = item.text.toString()
                editText.setText(pasteData)
                //Now check if the link is valid
            }
        }

        dLButton.setOnClickListener {
            //get user input
            string = editText.text.toString()
//            textView.text = string

            //parsing string and grab id of tweet
            val parse_question = string.split("?").toTypedArray()
            val parse_slash = parse_question[0].split("/").toTypedArray()
            val twitter_id = parse_slash.last()

            //feed id to function
            //now returns url of video, when given tweet ID, will be null if tweet has no video
            //GETS HIGHEST QUALITY ONLY FOR NOW
            val videoURL = test(twitter_id)
            val request = DownloadManager.Request(Uri.parse(videoURL))
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$twitter_id.mp4")
                .setTitle("$twitter_id.mp4")


            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this@MainActivity, "Downloading...", Toast.LENGTH_SHORT).show()
        }

    }
    
    
//    fun downloadFile(url: URL, fileName: String) {
//        url.openStream().use { inp ->
//            BufferedInputStream(inp).use { bis ->
//                FileOutputStream(fileName).use { fos ->
//                    val data = ByteArray(1024)
//                    var count: Int
//                    while (bis.read(data, 0, 1024).also { count = it } != -1) {
//                        fos.write(data, 0, count)
//                    }
//                }
//            }
//        }
//    }
    
    
//    private fun linkTextFocusListener()
//    {
//        binding.textInputEditText.
//        binding.textInputEditText.setOnFocusChangeListener { , focused ->
//            if(!focused)
//            {
//            }
//        }
//    }

    private fun validateLink()
    {

    }

    private fun test(id: String) = runBlocking() {
        val tweetMediaMap = twitterAPI.getTweetByID(id).mediaMap
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