package de.upb.cs.swt.delphi.webapi

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedStablePriorityMailbox}
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.{Enqueue, Retrieve}
import com.typesafe.config.Config

class ElasticPriorityMailbox (settings: ActorSystem.Settings, config: Config)
  extends UnboundedStablePriorityMailbox(
    PriorityGenerator{
      case Retrieve(_) => 5
      case Enqueue(_) => 1
      case _ => 2
    })
