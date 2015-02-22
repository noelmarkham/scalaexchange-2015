package com.noelmarkham.lambdadays.external

import scala.concurrent._

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

import scala.concurrent.ExecutionContext.Implicits.global

object Twitter {
  case class UserDetails(screenName: String)
  case class Tweet(user: UserDetails, content: String, published: String)

  implicit def detailsCodec: CodecJson[UserDetails] = casecodec1(UserDetails.apply, UserDetails.unapply)("screen_name")
  implicit def tweetCodec: CodecJson[Tweet] = casecodec3(Tweet.apply, Tweet.unapply)("user", "text", "created_at")

  def getTweets(twitterHandle: String, apiKey: String, apiSecret: String): Future[List[Tweet]] = {
    Future {
      val source = scala.io.Source.fromFile("tweets.json").mkString
      val tweets = Parse.decodeOption[List[Tweet]](source).getOrElse(throw new RuntimeException("Cannot parse response"))
      tweets.filter {
        case Tweet(UserDetails(screenName), _, _) => screenName === twitterHandle
      }
    }
  }
}
