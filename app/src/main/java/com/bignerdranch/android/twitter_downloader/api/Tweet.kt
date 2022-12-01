package com.bignerdranch.android.twitter_downloader.api
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import twitter4j.JSONObject
import twitter4j.TwitterFactory
import twitter4j.TwitterObjectFactory
import twitter4j.v2
import twitter4j.conf.ConfigurationBuilder


fun consumerKey() = "mN1pJ5Z2uIq4PSbQ8l2YcpqiY"
fun consumerSecret() = "DwsNLyytWv9ld1ELMxB76zKeUEIRmOBg3QPmAO5c7xXFi8mVZg"
fun accessToken() = "1592655216196800512-Ypd6x98FRvlaCXp0HGdDilelxiFfLD"
fun accessTokenSecret() = "nKkbNRezzZdHAKlE9jkuV5QV18zeVZMMx5h38AMo6mkVy"

private val TAG = Tweet::class.qualifiedName

class Tweet {
    companion object{
        suspend fun getTweetJSONByID(tweetID: String) = coroutineScope {
            withContext(Dispatchers.IO) {

                //authenticate
                val cb = ConfigurationBuilder()
                cb.setOAuthConsumerKey(consumerKey())
                cb.setOAuthConsumerSecret(consumerSecret())
                cb.setOAuthAccessToken(accessToken())
                cb.setOAuthAccessTokenSecret(accessTokenSecret())
                val twitter = TwitterFactory(cb.setJSONStoreEnabled(true).build()).instance

                return@withContext twitter.v2.getTweets(tweetID.toLong())
            }
        }
    }
}



