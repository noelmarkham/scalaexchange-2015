package com.noelmarkham.lambdadays.external

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random

import scalaz._
import Scalaz._
import argonaut._
import Argonaut._

import scala.concurrent.ExecutionContext.Implicits.global

object Twitter {
  case class UserDetails(screenName: String)
  case class Tweet(user: UserDetails, content: String)

  implicit def detailsCodec: CodecJson[UserDetails] = casecodec1(UserDetails.apply, UserDetails.unapply)("screen_name")
  implicit def tweetCodec: CodecJson[Tweet] = casecodec2(Tweet.apply, Tweet.unapply)("user", "text")

//  def help(handle: String): String = Await.result(getTweets(handle, "", ""), 1.second).map(_.content).mkString(" ")

  def getTweets(twitterHandle: String, apiKey: String, apiSecret: String): Future[List[Tweet]] = Future {
    val source = scala.io.Source.fromFile(s"${twitterHandle.toLowerCase}.json").mkString
    val tweets = Parse.decodeOption[List[Tweet]](source).getOrElse(throw new RuntimeException("Cannot parse response"))
    tweets.filter {
      case Tweet(UserDetails(screenName), content) => screenName.toLowerCase === twitterHandle.toLowerCase && !(content.startsWith("RT") || content.startsWith("@"))
    }
  }
}

object Markov {

  // point out that this has random generation in it, but this is the API we want to use
  def generateString(text: String, context: Int): Future[String] = Future {
    val tokens = text.split(" ").map(_.trim).filterNot(_ == "").toList

    val parts = tokens.sliding(context).toList

    val map: Map[List[String], List[String]] = parts.foldMap { words =>
      Map(words.init -> List(words.last))
    }

    val (startingWords, _) = map.toList(Random.nextInt(map.size))

    @tailrec
    def go(key: List[String], acc: List[String]): List[String] = {

      val nextWordList = map.get(key)

      nextWordList match {
        case Some(list) =>
          val nextWord = list(Random.nextInt(list.size))
          val nextKey = key.drop(1) ++ List(nextWord)
          go(nextKey, nextWord :: acc)
        case None => acc
      }

    }

    val sentence = startingWords ++ go(startingWords, Nil).reverse

    sentence.dropWhile(s => !s.charAt(0).isUpper).mkString(" ").split("(?<=[.?!] )").headOption.getOrElse("").trim.replace("\"", "")
  }
}