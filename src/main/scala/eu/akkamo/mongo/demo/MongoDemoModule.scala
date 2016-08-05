package eu.akkamo.mongo.demo

import java.util.Date

import akka.event.LoggingAdapter
import eu.akkamo
import eu.akkamo._
import eu.akkamo.mongo.{MongoApi, MongoModule}
import org.mongodb.scala.model._
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, MongoDatabase, Observable, _}

import scala.util.Try

class MongoDemoModule extends akkamo.Module with akkamo.Runnable {
  override def dependencies(dependencies: Dependency): Dependency = dependencies
    .&&[LogModule].&&[MongoModule]

  override def run(ctx: Context): Res[Context] = Try {
    val log: LoggingAdapter = ctx.get[LoggingAdapterFactory].apply(getClass)
    val mongoApi: MongoApi = ctx.get[MongoApi]

    log.info("Starting Mongo Demo application")
    doDemo(mongoApi.db)
    ctx
  }

  private def doDemo(db: MongoDatabase): Unit = {
    import Helpers._

    val collectionName: String = "test"

    def recreateCollection(): Observable[Completed] = {
      for {
        a <- db.getCollection(collectionName).drop()
        r <- db.createCollection(collectionName)
      } yield r
    }

    def insertSingleDocument(document: Document): Observable[Completed] = {
      db.getCollection(collectionName).insertOne(document)
    }

    def updateDocumentName(oldName: String, newName: String): Observable[UpdateResult] = {
      import org.mongodb.scala.model.Filters._
      import org.mongodb.scala.model.Updates._

      db.getCollection(collectionName).updateOne(equal("name", oldName), set("name", newName))
    }

    def deleteDocumentByName(name: String): Observable[DeleteResult] = {
      import org.mongodb.scala.model.Filters._
      db.getCollection(collectionName).deleteOne(equal("name", name))
    }

    def bulkWriteTest(): Observable[BulkWriteResult] = {
      import org.mongodb.scala.model.Updates._

      val writes: List[WriteModel[_ <: Document]] = List(
        InsertOneModel(Document("_id" -> 1)),
        InsertOneModel(Document("_id" -> 2)),
        InsertOneModel(Document("_id" -> 3)),
        InsertOneModel(Document("_id" -> 4)),
        InsertOneModel(Document("_id" -> 5)),
        InsertOneModel(Document("_id" -> 6)),
        UpdateOneModel(Document("_id" -> 1), set("x", 2)),
        DeleteOneModel(Document("_id" -> 2)),
        ReplaceOneModel(Document("_id" -> 3), Document("_id" -> 3, "x" -> 4))
      )

      db.getCollection(collectionName).bulkWrite(writes)
    }

    val document: Document = Document("currentDate" -> new Date().toString, "name" -> "first")

    val result1: Observable[Completed] = for {
      _ <- recreateCollection()
      r <- insertSingleDocument(document)
    } yield r
    result1.printHeadResult("\n\nDatabase recreated and test document added: ")

    updateDocumentName("first", "second").printHeadResult("Document successfully updated: ")

    deleteDocumentByName("second").printHeadResult("Document deleted: ")

    bulkWriteTest().printHeadResult("Bulk document write test done: ")

  }
}
