package com.bignerdranch.android.twitter_downloader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.twitter_downloader.api.getTweetJSONByID
import kotlinx.coroutines.runBlocking


private const val TAG = "Main Activity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test()
    }

    fun test() = runBlocking() {
        getTweetJSONByID("1591434056323203072")
    }


}