package de.upb.cs.swt.delphi.webapi.search

import spray.json.DefaultJsonProtocol

object QueryRequestJson extends DefaultJsonProtocol {
  implicit val queryRequestFormat = jsonFormat2(QueryRequest)
}
