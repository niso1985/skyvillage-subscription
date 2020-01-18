package controllers

import com.stripe.Stripe
import play.api.mvc._

object Application extends Controller {

  Stripe.apiKey = "sk_test_NdLJjjEOj59wG3YueEeohJ0y00ipviQfOy"

  def setup = Action {

    Ok("setup test")
  }
}
