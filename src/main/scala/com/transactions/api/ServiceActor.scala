package com.transactions.api

import akka.actor.{Props, ActorSystem, Actor}
import akka.io.IO
import spray.can.Http
import spray.http.{HttpMethods, Uri, HttpRequest, HttpResponse}
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.MongoClient
import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.util.Properties._
import HttpMethods._
import akka.pattern.ask
import akka.util.Timeout



case class TransactionReduced(_id: Int, transactionType: String, total: Double)
case class Transaction(_id: Int, transactionType: String, transactionDetails: String, total: Double) {
  require(!transactionType.isEmpty)
  require(!transactionDetails.isEmpty)
}

case class Journal(_id: Int, journalType: String, journalDetails: String, total: Double) {
  require(!journalType.isEmpty)
  require(!journalDetails.isEmpty)
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
          val journalObject = new Journal(transaction._id, transaction.transactionType, transaction.transactionDetails, transaction.total)
          implicit val system: ActorSystem = ActorSystem()
          implicit val timeout: Timeout = Timeout(15)
          import system.dispatcher
          val response: Future[HttpResponse] =
            (IO(Http) ? HttpRequest(GET, Uri("http://spray.io"))).mapTo[HttpResponse]
          println("Response   "+response.toString());
          complete(transaction)
        }
      }
    }
  }
}