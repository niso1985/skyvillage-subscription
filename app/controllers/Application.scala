package controllers

import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.param.CustomerCreateParams
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.{ PaymentMethodType, SubscriptionData }
import com.typesafe.config._
import models.{ CustomerInfo, ErrorResponse, StripeInfo }
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
    allCatch withTry (request.body.asJson.map(_.validate[CustomerInfo]) match {
      case None ⇒ throw new IllegalArgumentException("Need request body.")
      case Some(json) ⇒
        Logger.info(s"[createCheckoutSession] received json: ${json.toString}")
        json match {
          case e @ JsError(_) ⇒ throw new IllegalArgumentException(s"Json Parse error. ${e.toString}")
          case JsSuccess(customerInfo, _) ⇒
            Logger.info(s"[createCheckoutSession] received param: $customerInfo")
            val cbuilder = new CustomerCreateParams.Builder
            val cparam = cbuilder.setName(customerInfo.name)
              .setDescription(s"参加希望VILLAGE: ${customerInfo.village}")
              .setEmail(customerInfo.email)
              .build()
            val customer = Customer.create(cparam)
            Logger.info(s"[createCheckoutSession] created customer: $customer")

            val builder = new SessionCreateParams.Builder
            builder.setSuccessUrl(s"$baseUrl/assets/checkout/success.html")
              .setCancelUrl(s"$baseUrl/assets/checkout/canceled.html")
              .addPaymentMethodType(PaymentMethodType.CARD)
              .setCustomer(customer.getId)

            val planBuild = new SubscriptionData.Item.Builder()
              .setPlan(plan)
              .build
            val subscriptionData = new SubscriptionData.Builder()
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
}
