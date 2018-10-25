import akka.http.javadsl.model.StatusCodes
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.upb.cs.swt.delphi.featuredefinitions.FeatureListMapping
import de.upb.cs.swt.delphi.webapi.{BuildInfo, Server}

class ServerTests extends WordSpec with Matchers with ScalatestRouteTest
{

  "The Server" should { "return the version number" in {
    Get("version")-> Server.routes -> check{
      responseAs[String] shouldEqual(BuildInfo.version)
    } }

    "display features list" in {
      Get("features") -> Server.routes -> check{
          responseAs[String] shouldEqual(FeatureListMapping.featureList)
    }}
    "return Method not allowed error" in {
      Get("version") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
      Get("features") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("retrieve") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("enqueue") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("search") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("stop") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: POST"
      }
    }
  }
}