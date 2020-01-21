package controllers

import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.param.CustomerUpdateParams
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.{ PaymentMethodType, SubscriptionData }
import com.typesafe.config._
import models.{ ErrorResponse, StripeInfo, Village }
import play.Logger
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc._

import scala.util.control.Exception._
import scala.util.{ Failure, Success }

object Application extends Controller {
  val config: Config = ConfigFactory.load()
  val pubkey = config.getString("stripe.pubkey")
  val plan = config.getString("stripe.planid")
  val baseUrl = config.getString("baseurl")

  Stripe.apiKey = config.getString("stripe.secretkey")

  def setup = Action {
    val info = StripeInfo(pubkey, plan)
    Ok(Json.toJson(info))
  }

  def createCheckoutSession = Action { implicit request: Request[AnyContent] ⇒
    Logger.info(s"[createCheckoutSession] start")
    allCatch withTry (request.body.asJson.map(_.validate[Village]) match {
      case None ⇒ throw new IllegalArgumentException("Need request body.")
      case Some(json) ⇒
        Logger.info(s"[createCheckoutSession] received json: ${json.toString}")
        json match {
          case e @ JsError(_) ⇒ throw new IllegalArgumentException(s"Json Parse error. ${e.toString}")
          case JsSuccess(v, _) ⇒
            Logger.info(s"[createCheckoutSession] received param: $v")
            val builder = new SessionCreateParams.Builder
            builder.setSuccessUrl(s"$baseUrl/success/{CHECKOUT_SESSION_ID}")
              .setCancelUrl(s"$baseUrl/assets/checkout/canceled.html")
              .addPaymentMethodType(PaymentMethodType.CARD)

            val planBuild = new SubscriptionData.Item.Builder()
              .setPlan(plan)
              .build
            val subscriptionData = new SubscriptionData.Builder()
              .putMetadata("Village", v.village)
              .addItem(planBuild)
              .build
            builder.setSubscriptionData(subscriptionData)

            val createParams = builder.build
            val session = Session.create(createParams)
            Logger.info(s"[createCheckoutSession] created session: $session")

            Ok(Json.toJson(models.Session(session.getId)))
        }
    }) match {
      case Success(r) ⇒ r
      case Failure(ex) ⇒
        Logger.error(ex.getLocalizedMessage)
        BadRequest(Json.toJson(ErrorResponse(ex.getLocalizedMessage)))
    }
  }

  def updateCustomerMeta(sessionId: String) = Action { implicit request: Request[AnyContent] ⇒
    Logger.info(s"[updateCustomerMeta] session is $sessionId")
    allCatch withTry {
      val session = Session.retrieve(sessionId)
      Logger.info(s"[updateCustomerMeta] retrieve session: $session")
      val v = session.getSubscriptionObject.getMetadata.get("Village")
      Logger.info(s"[updateCustomerMeta] get metadata: ${v.toString}")
      val c = session.getCustomerObject
      Logger.info(s"[updateCustomerMeta] get customer object: ${c.toString}")
      val target = Customer.retrieve(c.getId)
      Logger.info(s"[updateCustomerMeta] retrieve customer: ${target.toString}")
      val p = new CustomerUpdateParams.Builder().putMetadata("Village", v).build
      val n = target.update(p)
      Logger.info(s"[updateCustomerMeta] updated customer meta: ${n.toString}")
      Redirect("/assets/checkout/success.html")
    } match {
      case Success(r) ⇒ r
      case Failure(ex) ⇒
        BadRequest(Json.toJson(ErrorResponse(ex.getLocalizedMessage)))
    }
  }
}
