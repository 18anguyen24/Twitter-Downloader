package com.bignerdranch .android.twitter_downloader

import android.app.DownloadManager
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bignerdranch.android.twitter_downloader.api.TwitterAPI
import com.bignerdranch.android.twitter_downloader.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import twitter4j.Media
import twitter4j.MediaKey
import twitter4j.TweetsResponse


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var dLButton: Button
    private lateinit var pasteButton: Button
    private lateinit var linkInputLayout: TextInputLayout
    private lateinit var mySpinner: Spinner
    private lateinit var binding: ActivityMainBinding
    private var twitterResponse: TweetsResponse? = null
    private var image: Bitmap? = null
    private var selectedBitrate: Int? = null
    private var tweetID: Long? = null
    private var videoKey: MediaKey? = null
    private var spinnerAdapter: ArrayAdapter<String>? = null
    private val twitterAPI = TwitterAPI()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        linkTextFocusListener()


        dLButton = findViewById(R.id.downloadButton)
        linkInputLayout = findViewById(R.id.linkContainer)
        pasteButton = findViewById(R.id.pasteButton)
        mySpinner = findViewById(R.id.qualitySpinner)

        linkInputLayout.isHintEnabled = false

        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData = ""

        val qualities2 = resources.getStringArray(R.array.video_quality_options)
        val qualities = qualities2.toMutableList()
        spinnerAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, qualities) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup,
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

        (spinnerAdapter as ArrayAdapter<String>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                id: Long,
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
                binding.textInputEditText.setText(pasteData)

                //remove previous bitrates if needed
                if(spinnerAdapter?.count!! >1)
                {
                    val end = spinnerAdapter?.count!!-1
                    Log.d(TAG, end.toString())
                    for(i in 1..end)
                    {
                        Log.d(TAG, i.toString())
                        val currentItem = spinnerAdapter!!.getItem(1)
                        spinnerAdapter!!.remove(currentItem)
                    }
                }

                //Now check if the link is valid
                binding.linkContainer.helperText = validateLink()
            }
        }

        binding.downloadButton.setOnClickListener { download() }

//        dLButton.setOnClickListener {
//            //get user input
//            string = editText.text.toString()
////            textView.text = string
//
//            //parsing string and grab id of tweet
//            val parse_question = string.split("?").toTypedArray()
//            val parse_slash = parse_question[0].split("/").toTypedArray()
//            val twitter_id = parse_slash.last()
//
//            //feed id to function
//            //now returns url of video, when given tweet ID, will be null if tweet has no video
//            //GETS HIGHEST QUALITY ONLY FOR NOW
//            val videoURL = test(twitter_id)
//            val request = DownloadManager.Request(Uri.parse(videoURL))
//                .setDescription("Downloading...")
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                .setAllowedOverMetered(true)
//                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$twitter_id.mp4")
//                .setTitle("$twitter_id.mp4")
//
//
//            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//            dm.enqueue(request)
//
//            Toast.makeText(this@MainActivity, "Downloading...", Toast.LENGTH_SHORT).show()
//        }

    }

    private fun download() = runBlocking {
        val validLink = binding.linkContainer.helperText == null
        //add another boolean here to check if quality is selected
        val qualitySelected = validateQuality()

        if(validLink)
        {
            //check if quality is selected
            if(qualitySelected)
            {
                //download video based on quality
                val a = twitterResponse?.mediaMap
                val b = a?.get(videoKey)
                val video = b?.asVideo
                val variants = video?.variants
                val selectedVariant = variants?.get(selectedBitrate!!-1)
                val downloadURL = selectedVariant?.url

                val request = DownloadManager.Request(Uri.parse(downloadURL))
                    .setDescription("Downloading...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$tweetID.mp4")
                    .setTitle("$tweetID.mp4")

                val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                Toast.makeText(this@MainActivity, "Downloading...", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this@MainActivity, "Select Quality First", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this@MainActivity, "Enter Valid Link First", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun linkTextFocusListener()
    {
        val window = this.window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val callBack = OnApplyWindowInsetsListener { view, insets ->
            val imeHeight = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom?:0
            Log.e("tag", "onKeyboardOpenOrClose imeHeight = $imeHeight")
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isKeyboardVisible) {
                // do something
                Log.d(TAG,"Keyboard open")
            }else{
                Log.d(TAG,"Keyboard closed")

                if(spinnerAdapter?.count!! >1)
                {
                    val end = spinnerAdapter?.count!!-1
                    Log.d(TAG, end.toString())
                    for(i in 1..end)
                    {
                        Log.d(TAG, i.toString())
                        val currentItem = spinnerAdapter!!.getItem(1)
                        spinnerAdapter!!.remove(currentItem)
                    }
                }
                binding.linkContainer.helperText = validateLink()
            }
            insets?: WindowInsetsCompat(null)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootConstraintLayout), callBack)

    }

    private fun validateLink(): String? = runBlocking{
        val linkText = binding.textInputEditText.text.toString()
        if (validLink(linkText))
        {
            return@runBlocking null
        }
        return@runBlocking "Invalid Link"
    }

    private suspend fun validLink(linkText: String): Boolean{

        val parse_question = linkText.split("?").toTypedArray()
        val parse_slash = parse_question[0].split("/").toTypedArray()
        val twitter_id = parse_slash.last()

        //give id to twitter API
        twitterResponse = twitterAPI.getTweetByID(twitter_id)
        if(twitterResponse == null)
        {
            return false
        }
        if(twitterResponse!!.errors.isNotEmpty())
        {
            return false
        }
        if (twitterResponse!!.mediaMap.isEmpty())
        {
            Toast.makeText(this@MainActivity, "Link has no video", Toast.LENGTH_SHORT).show()
            return false
        }
        for((key,value) in twitterResponse!!.mediaMap){
            if(value.type.toString()=="Video"){
                tweetID = twitter_id.toLong()
                //first add thumbnail?
                videoKey = key
                val a = value.asVideo
//                val urlString = a.previewImageUrl
//                Log.d(TAG,urlString)
//                //getBitmapFromURL(urlString)
//
//                binding.thumbnailImageView.setImageBitmap(image)

                //now add qualities
                val variants = a.variants
                val varIterator = variants.iterator()
                Log.d(TAG, variants.contentToString())
                while(varIterator.hasNext())
                {
                    val b = varIterator.next()
                    val bitrateString = b.bitRate.toString()
                    if(bitrateString != "null")
                    {
                        spinnerAdapter?.add(bitrateString)
                    }
                }
                return true
            }
        }
        Toast.makeText(this@MainActivity, "Link has no video", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun validateQuality(): Boolean{
        val item = mySpinner.selectedItemId
        if(item.toInt() == 0)
        {
            return false
        }
        selectedBitrate = item.toInt()
        return true
    }

//    private fun getBitmapFromURL(src: String?) {
//        CoroutineScope(Job() + Dispatchers.IO).launch {
//            try {
//                val url = URL(src)
//                Log.d(TAG,"testing1")
//                val bitMap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//                image = Bitmap.createScaledBitmap(bitMap, 100, 100, true)
//                Log.d(TAG,"testing2")
//            } catch (e: Throwable) {
//                // Log exception
//                Log.d(TAG,"testing3")
//            }
//            Log.d(TAG,"testing4")
//        }
//        Log.d(TAG,"testing6")
//    }

//    private fun getBitmapFromURL2(src: String?){
//        CoroutineScope(Job() + Dispatchers.IO).launch {
//            try {
//                Log.d(TAG,"testing1")
//                val url = URL(src)
//                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
//                connection.setDoInput(true)
//                connection.connect()
//                Log.d(TAG,"testing2")
//                val input: InputStream = connection.getInputStream()
//                Log.d(TAG,"testing2")
//                image = BitmapFactory.decodeStream(input)
//            } catch (e: IOException) {
//                // Log exception
//                Log.d(TAG,"testing3")
//            }
//        }
//
//    }

    private suspend fun getTweet(id: String): TweetsResponse? {
        val tweet = twitterAPI.getTweetByID(id)
        //val testKeyToMake = MediaKey("3_1598345418198470657")
        //val hasKey = testVal.containsKey(testKeyToMake)
        return tweet

//        //key value for media that is the video, could be missing
//        var keyForVideo:MediaKey?=null
//
//        if(tweetMediaMap.isNotEmpty()) {
//            for ((key,value) in tweetMediaMap) {
//                if(value.type.toString() == "Video"){
//                    keyForVideo = key
//                }
//            }
//            if (keyForVideo != null) {
//                val value = tweetMediaMap.get(keyForVideo)
//                val videoInfo = value?.asVideo
//                val variants = videoInfo?.variants
//                //WRITE FUNCTION CALL OR SOMETHING THAT GETS VIDEO BASED ON QUALITY?
//                val firstVariant = variants?.get(variants.size-2)
//                val variantUrl = firstVariant?.url
//                Log.d(TAG, variantUrl!!)
//                return@runBlocking variantUrl
//            }
//            else{
//                //TWEET IS MISSING VIDEO
//                return@runBlocking null
//            }
//        }
//        else{
//            //TWEET IS MISSING MEDIA
//            return@runBlocking null
//        }
    }

}