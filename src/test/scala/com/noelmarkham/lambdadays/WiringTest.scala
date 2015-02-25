package com.noelmarkham.lambdadays

import com.noelmarkham.lambdadays.external.Twitter.Tweet
import com.noelmarkham.lambdadays.external.{Markov, Twitter}

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import Scalaz._

class WiringTest extends org.scalatest.FunSuite {

  val apiKey = "APIKey"
  val apiSecret = "APISecret"
  val context = 2

  test("Step 1: Wiring the methods together") {

    def randomStringFromTwitterUser(username: String): Future[String] = for {
      tweets <- Twitter.getTweets(username, apiKey, apiSecret)
      tweetsAsText = tweets.map(_.content).mkString(" ")
      randomString <- Markov.generateString(tweetsAsText, context)
    } yield randomString

  }

  test("Step 2: Extract the configuration") {
    case class Config(key: String, secret: String, context: Int)

    import Kleisli._

    def getTweets(username: String): ReaderT[Future, Config, List[Twitter.Tweet]] = {
      kleisli { c =>
        Twitter.getTweets(username, c.key, c.secret)
      }
    }
    def generateString(text: String): ReaderT[Future, Config, String] = {
      kleisli { c =>
        Markov.generateString(text, c.context)
      }
    }



    def randString(username: String): ReaderT[Future, Config, String] = {
      for {
        tweets <- getTweets(username)
        tweetsAsText = tweets.map(_.content).mkString(" ")
        randomString <- generateString(tweetsAsText)
      } yield randomString
    }

//    def contextFree(username: String): Future[String] = for {
//      ts <- Twitter.getTweets(username, apiKey, apiSecret)
//      tweetsAsText = tweets.map(_.content).mkString(" ")
//      r <- Markov.generateString(tweetsAsText, context)
//    } yield r
  }

}
