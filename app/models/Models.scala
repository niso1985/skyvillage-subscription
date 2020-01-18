package models

import play.api.libs.json.Json

case class StripeInfo(
    publicKey: String,
    plan:      String
)
object StripeInfo {
  implicit val jw = Json.writes[StripeInfo]
}

case class Session(sessionId: String)
object Session {
  implicit val jw = Json.writes[Session]
}
