package com.bignerdranch.android.twitter_downloader.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import twitter4j.TwitterFactory
import twitter4j.v2
import twitter4j.JSONObject
import twitter4j.TwitterObjectFactory
import twitter4j.conf.ConfigurationBuilder




fun consumerKey() = System.getenv("Twitter_API_Key")
fun consumerSecret() = System.getenv("Twitter_API_Secret_Key")
fun accessToken() = System.getenv("Twitter_Access_Token")
fun accessTokenSecret() = System.getenv("Twitter_Access_Secret_Token")

suspend fun getTweetJSONByID(tweetID: String) = coroutineScope {
    withContext(Dispatchers.IO){

        //authenticate
        val cb = ConfigurationBuilder()
        cb.setOAuthConsumerKey(consumerKey())
        cb.setOAuthConsumerSecret(consumerSecret())
        cb.setOAuthAccessToken(accessToken())
        cb.setOAuthAccessTokenSecret(accessTokenSecret())
        val twitter = TwitterFactory(cb.setJSONStoreEnabled(true).build()).instance

        twitter.v2.getTweets(tweetID.toLong()).let {
            println(it)

            val json = JSONObject(TwitterObjectFactory.getRawJSON(it))
            println(json.toString(3))
        }
    }
}
