package io.astralforge.astraltech.tile

class Box constructor(
    private val start: XY<Int>,
    private val end: XY<Int>
) {

  operator fun contains(element: Int?): Boolean {
    if (element == null) {
      return false
    }

    if ((element % 9).let { it < start.x || it > end.x }) {
      return false
    }
    if ((element / 9).let { it < start.y || it > end.y }) {
      return false
    }

    return true
  }

  fun getBox(): List<Int> {
    val box = mutableListOf<Int>()
    for (y in start.y..end.y) {
      for (x in start.x..end.x) {
        box.add((y*9) + x)
      }
    }

    return box
  }

}

data class XY<T>(val x: T, val y: T)

inline fun <T> Iterable<T>.notAll(
    predicate: (T) -> Boolean
): Boolean {
  return !all(predicate)
}
