package nl.jhvh.eratosthenes.classic

import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * Implements the classic classic [sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes) algorithm.
 * Allocates all possible candidate values in an [Array] and eliminates non-primes by removing all multipliers
 * of preceding primes.
 *
 * > Pro:
 *  * Simple algorithm
 *  * Fast! primes in range 2 .. 10M: less than 200ms; primes in range 2 .. 100M: less then 5s ! on my current laptop
 * > Con:
 * * consumes lots of memory (max allocated heap memory):
 *   * primes in range 2 .. 1M: ~ 4.6 MB
 *   * primes in range 2 .. 100M: ~ 290 MB
 *   * primes in range 2 .. 300M: ~ 3 GB !!
 *   > (and something like 4.4 MB of non-heap memory)
 *   > higher ranges, say 2 .. 400M, will crash with [OutOfMemoryError]
 * * by nature of the Eratosthenes algorithm, you can only find a higher prime
 *   by determining all preceding primes down to 2 as well
 *
 * > The candidate values are all integers in the range `2..`[maxNum], inclusive.
 * @param maxNum The end value of the range of candidate values
 */
open class Eratosthenes(val maxNum: Int) {
    init {
        println("Primes up to: $maxNum")
        print("Start: ")
        printMemUsage()
    }

    // upfront optimization: skipping the even numbers
    private val candidates: Array<Int?> = Array((maxNum+1)/2) { index -> if (index == 0) 2 else index*2 + 1 }

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    /**
     * Implementation of sieve of Eratosthenes, with the following optimizations:
     * * The candidate primes to check are limited to the [sqrt] of [maxNum]
     *   * all values between [sqrt] ([maxNum]) and [maxNum] are either eliminated or primes
     * * The multiplier starts with the candidate value, not with 2
     *   * the preceding values are known to have been eliminated already
     * * Even numbers except 2 are excluded beforehand
     *   * this is NOT transparent for this method (impacts the calculation of the index of the [candidates] array)
     */
    fun sift(): Sequence<Int> {
        val maxPrimeToTry = sqrt(maxNum.toDouble()).toInt()
            try {
            candidates
                .asSequence()
                .filterNotNull()
                .filter { it in 3..maxPrimeToTry }
                .forEach { candidate ->
                    var multiplier = candidate
                    while (candidate * multiplier <= maxNum) {
                        candidates[candidate * multiplier/2] = null
                        multiplier += 2
                    }
                }
        } finally {
            print("After primes calculated: ")
            printMemUsage()
//            val primeList = candidates.asSequence().filterNotNull().toList()
//            println("last 5 primes: ${primeList.filterIndexed { index, candidate -> index >= primeList.size-6 }}")
        }
        return candidates.asSequence().filterNotNull()
    }
}

fun main() {
    println(">>> classic Eratosthenes (in memory)")
    Eratosthenes(100).sift().forEach { println(it) }

    println("... classic Eratosthenes (in memory)")
    var startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(1_000_000).sift().count()}")
    var endTime = System.currentTimeMillis()
    println("classic Eratosthenes (in memory) - elapsed ms: ${endTime - startTime}")

    println("... classic Eratosthenes (in memory)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(10_000_000).sift().count()}")
    endTime = System.currentTimeMillis()
    println("classic Eratosthenes (in memory) - elapsed ms: ${endTime - startTime}")

    println("... classic Eratosthenes (in memory)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(100_000_000).sift().count()}")
    endTime = System.currentTimeMillis()
    println("classic Eratosthenes (in memory) - elapsed ms: ${endTime - startTime}")

    println("... classic Eratosthenes (in memory)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(300_000_000).sift().count()}")
    endTime = System.currentTimeMillis()
    println("classic Eratosthenes (in memory) - elapsed ms: ${endTime - startTime}")

    println("### classic Eratosthenes (in memory) ###")
}