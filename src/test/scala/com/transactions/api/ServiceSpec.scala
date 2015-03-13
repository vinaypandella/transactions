package com.transactions.api

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeExample
import spray.http.{ContentType, HttpEntity}
import spray.http.MediaTypes._
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._

import spray.httpx.SprayJsonSupport._

class ServiceSpec extends Specification with Specs2RouteTest with HttpService with ServiceRoute with BeforeExample {

  val client = MongoClient("localhost", 27017)
  val db = client("transactions")
  val collection = db("transactions")
  val uri = "/api/v1/transactions"
  val transactionId = 1234

  def actorRefFactory = system
  def before = db.dropDatabase()

  sequential

  s"GET $uri" should {

    "return OK" in {
      Get(uri) ~> route ~> check {
        response.status must equalTo(OK)
      }
    }

    "return all transactions" in {
      val expected = insertTransactions(3).map { transaction =>
        TransactionReduced(transaction._id, transaction.transactionType, transaction.total)
      }
      Get(uri) ~> route ~> check {
        response.entity must not equalTo None
        val transactions = responseAs[List[TransactionReduced]]
        transactions must haveSize(expected.size)
        transactions must equalTo(expected)
      }
    }

  }

  s"GET $uri/_id/$transactionId" should {

    val expected = Transaction(transactionId, "SALE", "Transaction Details", 99.99)

    "return OK" in {
      insertTransaction(expected)
      Get(s"$uri/_id/$transactionId") ~> route ~> check {
        response.status must equalTo(OK)
      }
    }

    "return transaction" in {
      insertTransaction(expected)
      Get(s"$uri/_id/$transactionId") ~> route ~> check {
        response.entity must not equalTo None
        val transaction = responseAs[Transaction]
        transaction must equalTo(expected)
      }
    }

  }

  s"PUT $uri" should {

    val expected = Transaction(transactionId, "RETURN", "Transaction Details", 30.09)

    "return OK" in {
      Put(uri, expected) ~> route ~> check {
        response.status must equalTo(OK)
      }
    }

    "return Transaction" in {
      Put(uri, expected) ~> route ~> check {
        response.entity must not equalTo None
        val transaction = responseAs[Transaction]
        transaction must equalTo(expected)
      }
    }

    "insert transaction to the DB" in {
      Put(uri, expected) ~> route ~> check {
        response.status must equalTo(OK)
        val transaction = getTransaction(transactionId)
        transaction must equalTo(expected)
      }
    }

    "update transaction when it exists in the DB" in {
      collection.insert(grater[Transaction].asDBObject(expected))
      Put(uri, expected) ~> route ~> check {
        response.status must equalTo(OK)
        val transaction = getTransaction(transactionId)
        transaction must equalTo(expected)
      }
    }

  }

  s"DELETE $uri/_id/$transactionId" should {

    val expected = Transaction(transactionId, "SALE", "Transaction Details", 98.99)

    "return OK" in {
      insertTransaction(expected)
      Delete(s"$uri/_id/$transactionId") ~> route ~> check {
        response.status must equalTo(OK)
      }
    }

    "return transaction" in {
      insertTransaction(expected)
      Delete(s"$uri/_id/$transactionId") ~> route ~> check {
        response.entity must not equalTo None
        val transaction = responseAs[Transaction]
        transaction must equalTo(expected)
      }
    }

    "remove transaction from the DB" in {
      insertTransaction(expected)
      Delete(s"$uri/_id/$transactionId") ~> route ~> check {
        response.status must equalTo(OK)
        getTransactions must haveSize(0)
      }
    }

  }

  def insertTransaction(transaction: Transaction) {
    collection.insert(grater[Transaction].asDBObject(transaction))
  }

  def insertTransactions(quantity: Int): List[Transaction] = {
    val transactions = List.tabulate(quantity)(id => Transaction(id, s"TransactionType $id", s"TransactionDetails $id", 30))
    for (transaction <- transactions) {
      collection.insert(grater[Transaction].asDBObject(transaction))
    }
    transactions
  }

  def getTransaction(id: Int): Transaction = {
    val dbObject = collection.findOne(MongoDBObject("_id" -> id))
    grater[Transaction].asObject(dbObject.get)
  }

  def getTransactions: List[Transaction] = {
    collection.find().toList.map(grater[Transaction].asObject(_))
  }

}
