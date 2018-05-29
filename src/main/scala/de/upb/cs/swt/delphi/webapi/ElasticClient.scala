package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient

object ElasticClient {

  val configuration = new Configuration()
  val client = HttpClient(configuration.elasticsearchClientUri)
  val index = "delphi" / "project"

  //Returns an entry with the given ID as an option
  def getSource(id: String) =
    client.execute{
      get(id).from(index)
    }.await match {
      case Right(res) => res.body
      case Left(_) => Option.empty
    }
}
