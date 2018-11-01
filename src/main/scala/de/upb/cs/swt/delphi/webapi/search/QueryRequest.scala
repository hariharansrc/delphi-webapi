package de.upb.cs.swt.delphi.webapi.search

case class QueryRequest (query : String, limit : Option[Int] = Some(50))
