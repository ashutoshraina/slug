package org.slug.core

import java.util.*

class PowerLaw {

  private var rand: Random? = null

  constructor(r: Random) {
    rand = r
  }

  constructor() {
    rand = Random()
  }

  fun getRand(): Double {
    return rand!!.nextDouble()
  }

  fun getRandInt(N: Int): Int {
    return rand!!.nextInt(N)
  }

  fun select(doubles: DoubleArray, p: Double): Int {
    // make array of probabilities
    val probabilities = DoubleArray(doubles.size)
    for (i in probabilities.indices) {
      if (doubles[i] == 0.0)
        probabilities[i] = 0.0
      else
        probabilities[i] = Math.pow(doubles[i], p)
    }

    // sum probabilities
    val sum = probabilities.indices.sumByDouble { probabilities[it] }

    // obtain random number in range [0, sum]
    var r = sum * getRand()

    var i: Int = 0
    while (i < probabilities.size) {
      r -= probabilities[i]
      if (r < 0) {
        break
      }
      i++
    }
    return i
  }

  fun zipf(size: Int): Int {
    val numbers = DoubleArray(size)
    for (i in numbers.indices) {
      numbers[i] = (i + 1).toDouble()
    }
    return select(numbers, -1.0) + 1
  }

}