package de.innfactory.play.mail

import com.typesafe.config.Config

import javax.inject.Inject

trait MailConfig {
  def endpoint: String
  def apiKey: String
}

class DefaultMailConfig @Inject()(config: Config) extends MailConfig {
  def endpoint: String = config.getString("mail.endpoint")
  def apiKey: String = config.getString("mail.apiKey")
}
