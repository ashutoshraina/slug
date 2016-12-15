package org.slug.core

import java.util.Random

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

    fun select(nums: DoubleArray, p: Double): Int {
        // make array of probabilities
        val probs = DoubleArray(nums.size)
        for (i in probs.indices) {
            if (nums[i] == 0.0)
                probs[i] = 0.0
            else
                probs[i] = Math.pow(nums[i], p)
        }

        // sum probabilities
        var sum = 0.0
        for (i in probs.indices) {
            sum += probs[i]
        }

        // obtain random number in range [0, sum]
        var r = sum * getRand()

        var i: Int
        i = 0
        while (i < probs.size) {
            r -= probs[i]
            if (r < 0) {
                break
            }
            i++
        }
        return i
    }

    fun zipf(size: Int): Int {
        // make array of numbers
        val nums = DoubleArray(size)
        for (i in nums.indices) {
            nums[i] = (i + 1).toDouble()
        }
        // get index using special case of power law
        return select(nums, -1.0)
    }

}