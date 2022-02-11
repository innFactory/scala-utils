package de.innfactory.play.controller

abstract class ErrorResult() extends ResultStatus {
  def message: String
  def additionalInfoToLog: Option[String]
  def additionalInfoErrorCode: Option[String]
  def statusCode: Int
}