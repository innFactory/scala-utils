package de.innfactory.play.mail

import com.typesafe.config.Config

case class MailConfig(endpoint: String, apiKey: String)

object MailConfig {
  def default()(implicit config : Config) = MailConfig(config.getString("mail.endpoint"), config.getString("mail.apiKey"))
}