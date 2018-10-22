// Copyright (C) 2018 The Delphi Team.
// See the LICENCE file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.upb.cs.swt.delphi.webapi


import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.http.scaladsl.model.RemoteAddress
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.ElasticMessage
import de.upb.cs.swt.delphi.webapi.ElasticRequestLimiter._

import scala.collection.mutable
import scala.concurrent.duration._

//Limits the number of requests any given IP can make by tracking how many requests an IP has made within a given
//  window of time, and timing out any IP that exceeds a threshold by rejecting any further request for a period of time
class ElasticRequestLimiter(configuration: Configuration, nextActor: ActorRef) extends Actor with ActorLogging with Timers {

  private val window = 1 second
  private val threshold = 10
  private val timeout = 2 hours

  private var recentIPs: mutable.Map[String, Int] = mutable.Map()
  private var blockedIPs: mutable.Set[String] = mutable.Set()

  override def preStart(): Unit = {
    log.info("Request limiter started")
    timers.startPeriodicTimer(ClearTimer, ClearLogs, window)
  }
  override def postStop(): Unit = log.info("Request limiter shut down")

  override def receive = {
    case Validate(rawIp, message) => {
      val ip = rawIp.toOption.map(_.getHostAddress).getOrElse("unknown")
      //First, reject IPs marked as blocked
      if (blockedIPs.contains(ip)) {
        rejectRequest()
      } else {
        //Check if this IP has made any requests recently
        if (recentIPs.contains(ip)) {
          //If so, increment their counter and test if they have exceeded the request threshold
          recentIPs.update(ip, recentIPs(ip) + 1)
          if (recentIPs(ip) > threshold) {
            //If the threshold has been exceeded, mark this IP as blocked and reject it, and set up a message to unblock it after a period
            blockedIPs += ip
            log.info("Blocked IP {} due to exceeding request frequency threshold", ip)
            timers.startSingleTimer(ForgiveTimer(ip), Forgive(ip), timeout)
            rejectRequest()
          } else {
            //Else, forward this message
            nextActor forward message
          }
        } else {
          //Else, register their request in the map and pass it to the next actor
          recentIPs += (ip -> 1)
          nextActor forward message
        }
      }
    }
    case ClearLogs =>
      recentIPs.clear()
    case Forgive(ip) => {
      blockedIPs -= ip
      log.info("Forgave IP {} after timeout", ip)
    }
  }

  //Rejects requests from blocked IPs
  private def rejectRequest() =
    sender() ! "Sorry, you have exceeded the limit on request frequency for unregistered users.\n" +
      "As a result, you have been timed out.\n" +
      "Please wait a while or register an account with us to continue using this service."
}

object ElasticRequestLimiter{
  def props(configuration: Configuration, nextActor: ActorRef) : Props = Props(new ElasticRequestLimiter(configuration, nextActor))

  final case class Validate(rawIp: RemoteAddress, message: ElasticMessage)
  final case object ClearLogs
  final case class Forgive(ip: String)

  final case object ClearTimer
  final case class ForgiveTimer(ip: String)
}