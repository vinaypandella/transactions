package com.technologyconversations.api

import akka.actor.Actor
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.MongoClient
import scala.util.Properties._

case class TransactionReduced(_id: Int, transactionType: String, total: Double)
case class Transaction(_id: Int, transactionType: String, transactionDetails: String, total: Double) {
  require(!transactionType.isEmpty)
  require(!transactionDetails.isEmpty)
}

class ServiceActor extends Actor with ServiceRoute {

  val client = MongoClient(
    envOrElse("DB_PORT_27017_TCP_ADDR", "localhost"),
    envOrElse("DB_PORT_27017_TCP_PORT", "27017").toInt
  )
  val db = client(envOrElse("DB_DBNAME", "transactions"))
  val collection = db(envOrElse("DB_COLLECTION", "transactions"))

  def actorRefFactory = context
  def receive = runRoute(route)

}

trait ServiceRoute extends HttpService with DefaultJsonProtocol {

  implicit val transactionsReducedFormat = jsonFormat3(TransactionReduced)
  implicit val transactionsFormat = jsonFormat4(Transaction)
  val collection: MongoCollection

  val route = pathPrefix("api" / "v1" / "transactions") {
    path("_id" / IntNumber) { id =>
      get {
        complete(
          grater[Transaction].asObject(
            collection.findOne(MongoDBObject("_id" -> id)).get
          )
        )
      } ~ delete {
        complete(
          grater[Transaction].asObject(
            collection.findAndRemove(MongoDBObject("_id" -> id)).get
          )
        )
      }
    } ~ pathEnd {
      get {
        complete(
          collection.find().toList.map(grater[TransactionReduced].asObject(_))
        )
      } ~ put {
        entity(as[Transaction]) { transaction =>
          collection.update(
            MongoDBObject("_id" -> transaction._id),
            grater[Transaction].asDBObject(transaction),
            upsert = true
          )
          complete(transaction)
        }
      }
    }
  }

}