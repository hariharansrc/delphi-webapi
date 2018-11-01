package de.upb.cs.swt.delphi.webapi

import org.scalatest.{FlatSpec, Matchers}

class StatisticsQueryCheck extends FlatSpec with Matchers {
  "Statics" should "be retrievable" in {
    val configuration = new Configuration()
    val stats = new StatisticsQuery(configuration)
    println(stats.retrieveStandardStatistics)
  }
}
