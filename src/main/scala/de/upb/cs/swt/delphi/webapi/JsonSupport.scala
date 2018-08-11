package de.upb.cs.swt.delphi.webapi

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol