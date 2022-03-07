package de.innfactory.play.results

import de.innfactory.play.controller.ResultStatus

object Results {

  type Result[T] = Either[ResultStatus, T]

}
