package controllers

import com.stripe.Stripe
import com.typesafe.config._
import models.StripeInfo
import play.api.mvc._
import play.api.libs.json.Json

object Application extends Controller {
  val config: Config = ConfigFactory.load()

  Stripe.apiKey = config.getString("stripe.secretkey")

  def setup = Action {
    val info = StripeInfo(
      config.getString("stripe.pubkey"),
      config.getString("stripe.planid")
    )
    Ok(Json.toJson(info))
  }
}
