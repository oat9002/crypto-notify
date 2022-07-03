//import akka.actor.testkit.typed.scaladsl.ActorTestKit
//import commons.{Configuration, HttpClient}
//import models.bscScan.BscScanResponse
//import org.scalamock.scalatest.MockFactory
//import org.scalatest.funspec.AnyFunSpec
//import org.scalatest.matchers.should.Matchers
//import services.BscScanServiceImpl
//
//import scala.concurrent.duration.Duration
//import scala.concurrent.{Await, Future}
//
//class BscScanServiceSpec extends AnyFunSpec with Matchers with MockFactory {
//  val actorTestKit: ActorTestKit = ActorTestKit()
//  val httpClientMock: HttpClient = mock[HttpClient]
//  val configurationMock: Configuration = mock[Configuration]
//  val bscScanService = new BscScanServiceImpl(
//    configurationMock,
//    httpClientMock
//  )(actorTestKit.system, actorTestKit.system.executionContext)
//
//  describe("getBnbBalance") {
//    it("get balance correctly") {
//
//      (httpClientMock.get[Any, BscScanResponse] _).expects(*, *, *).returning(Future.successful(Right(BscScanResponse(200, "", BigInt(1)))))
//
//      val result = Await.result(bscScanService.getBnbBalance("address"), Duration.Inf)
//
//      result shouldBe Some(BigInt(1))
//    }
//  }
//}
