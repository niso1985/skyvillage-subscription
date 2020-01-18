package controllers

import com.stripe.Stripe
import com.typesafe.config._
import models.StripeInfo
import play.api.mvc._
import play.api.libs.json.Json
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.PaymentMethodType
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData

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

  def createCheckoutSession = Action {
    val builder = new SessionCreateParams.Builder
    builder.setSuccessUrl(s"$baseUrl/success.html?session_id={CHECKOUT_SESSION_ID}")
      .setCancelUrl(s"$baseUrl/canceled.html")
      .addPaymentMethodType(PaymentMethodType.CARD)

    val planBuild = new SubscriptionData.Item.Builder().setPlan(plan).build
    val subscriptionData = new SubscriptionData.Builder().addItem(planBuild).build
    builder.setSubscriptionData(subscriptionData)

    val createParams = builder.build
    val session = Session.create(createParams)

    Ok(Json.toJson(models.Session(session.getId)))
  }
}
