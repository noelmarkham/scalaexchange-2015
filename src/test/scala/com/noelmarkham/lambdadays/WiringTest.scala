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

  case class Config(key: String, secret: String, context: Int)

  test("Step 1: Wiring the methods together") {

    def randomStringFromTwitterUser(username: String): Future[String] = for {
      tweets <- Twitter.getTweets(username, apiKey, apiSecret)
      tweetsAsText = tweets.map(_.content).mkString(" ")
      randomString <- Markov.generateString(tweetsAsText, context)
    } yield randomString

  }

  test("Step 2: Extract the configuration") {


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

  test("Step 3: Testing") {

    def randString(getTweetsF: String => ReaderT[Future, Config, List[Tweet]],
                   generateTextF: String => ReaderT[Future, Config, String]
                  )(username: String):
    ReaderT[Future, Config, String] = {
      for {
        tweets <- getTweetsF(username)
        tweetsAsText = tweets.map(_.content).mkString(" ")
        randomString <- generateTextF(tweetsAsText)
      } yield randomString
    }

    val prop = forAll { (tweets: List[Tweet]) =>
      def tweetsR(name: String):
      ReaderT[Future, Config, List[Tweet]] = {
        kleisli { _ => Future(tweets) }
      }
      def textR(text: String):
      ReaderT[Future, Config, String] = {
        kleisli { _ => Future(text) }
      }
      val r = randString(tweetsR, textR)("name")
      val receivedText =
        Await.result(r.run(Config("a", "b", 2)), 1.second)

      tweets.all(t => receivedText.contains(t.content))
    }
  }

  test("Step 4: Abstraction") {
    def randString[M[_]: Monad](getTweetsF: String => M[List[Tweet]],
                             generateTextF: String => M[String]
                            )(username: String): M[String] = {
      for {
        tweets <- getTweetsF(username)
        tweetsAsText = tweets.map(_.content).mkString(" ")
        randomString <- generateTextF(tweetsAsText)
      } yield randomString
    }

    val prop = forAll { (tweets: List[Tweet]) =>
      def tweetsF(name: String): Id[List[Tweet]] = tweets
      def textF(text: String): Id[String] = text

      val receivedText = randString(tweetsF, textF)("name")

      tweets.all(t => receivedText.contains(t.content))
    }
  }
}
