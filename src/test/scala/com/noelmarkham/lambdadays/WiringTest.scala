package com.noelmarkham.lambdadays

import com.noelmarkham.lambdadays.external.{Markov, Twitter}

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

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

}
