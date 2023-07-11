package de.innfactory.play.common

final class PlayNativeLoader {

  // native library and play class loader must be same. Thus loadLibrary must be called from a jar.
  // https://github.com/playframework/playframework/issues/2212#issuecomment-58824866
  def load(name: String): Unit =
    System.loadLibrary(name)

}
