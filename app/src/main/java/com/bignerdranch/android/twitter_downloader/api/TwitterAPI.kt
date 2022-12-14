package com.bignerdranch.android.twitter_downloader.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import twitter4j.TwitterFactory
import twitter4j.v2
import twitter4j.conf.ConfigurationBuilder


fun consumerKey() = "mN1pJ5Z2uIq4PSbQ8l2YcpqiY"
fun consumerSecret() = "DwsNLyytWv9ld1ELMxB76zKeUEIRmOBg3QPmAO5c7xXFi8mVZg"
fun accessToken() = "1592655216196800512-Ypd6x98FRvlaCXp0HGdDilelxiFfLD"
fun accessTokenSecret() = "nKkbNRezzZdHAKlE9jkuV5QV18zeVZMMx5h38AMo6mkVy"

private val TAG = TwitterAPI::class.qualifiedName

class TwitterAPI {
    private val cb = ConfigurationBuilder()

    init {
        //authenticate
        cb.setOAuthConsumerKey(consumerKey())
        cb.setOAuthConsumerSecret(consumerSecret())
        cb.setOAuthAccessToken(accessToken())
        cb.setOAuthAccessTokenSecret(accessTokenSecret())
    }
    suspend fun getTweetByID(tweetID: String) = coroutineScope {
        withContext(Dispatchers.IO) {
            val twitter = TwitterFactory(cb.setJSONStoreEnabled(true).build()).instance
            return@withContext twitter.v2.getTweets(tweetID.toLong())
        }
    }
}



