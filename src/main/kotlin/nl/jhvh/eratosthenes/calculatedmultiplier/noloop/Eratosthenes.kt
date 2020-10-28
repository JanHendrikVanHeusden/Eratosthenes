package nl.jhvh.eratosthenes.calculatedmultiplier.noloop

import nl.jhvh.eratosthenes.calculatedmultiplier.noloop.Eratosthenes.Prime
import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * An alternative implementation that basically follows the classic sieve of Eratosthenes algorithm
 * (see [nl.jhvh.eratosthenes.classic.Eratosthenes]), but instead of holding all values in memory, the prime's
 * multiplier is calculated (and its current value remembered as [Prime.currentMultiple])
 * > See also [nl.jhvh.eratosthenes.calculatedmultiplier.Eratosthenes], this is identical but with
 *   a slightly different and apparently somewhat faster implementation of [Prime.isDivisorOf]
 *
 * Pros & cons of this approach, compared to the "classic" in-memory approach of [nl.jhvh.eratosthenes.classic.Eratosthenes]:
 * > Pro:
 * * consumes less heap memory (up to 6 times less for higher counts!) than classic approach:
 *   * primes in range 2 .. 1M: ~ 3.6 MB
 *   * primes in range 2 .. 100M: ~ 170 MB
 *   * primes in range 2 .. 300M: ~ 450 MB
 *   > (and something like 4.4 MB of non-heap memory)
 * > Con:
 *  * for higher counts up to 10 times slower than the classic "in memory" approach: 2 .. 10M in ~ 263ms, 2 .. 100M in ~ 30 to 45s
 *    on my current laptop
 *  * algorithm somewhat more complex and less elegant, calculation of multiplier not straightforward on first sight
 *  * by nature of the Eratosthenes algorithm, you can only find a higher prime
 *    by determining all preceding primes down to 2 as well
 * > The candidate values are all integers in the range `2..`[maxNum], inclusive.
 * @param maxNum The end value of the range of candidate values
 */
class Eratosthenes(val maxNum: Int) {
    init {
        println("Primes up to: $maxNum")
        print("Start: ")
        printMemUsage()
    }

    private val primes: MutableList<Prime> = mutableListOf(Prime(2))
    private val candidates = 3..maxNum step 2 // upfront optimization: skipping the even numbers

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    /**
     * Mutable stateful class representing a prime and the current value of the multiplier
     * to use for sifting the subsequent higher candidate values
     * @param prime the prime value of this class
     */
    private class Prime(val prime: Int) {

        private var currentMultiple: Int = prime

        /** Check if this [prime] is a divisor of [candidateToCheck] */
        infix fun isDivisorOf(candidateToCheck: Int): Boolean {
            // This could also be accomplished by repeatedly adding the prime
            // in a loop until currentMultiple >= candidateToCheck
            currentMultiple += (((candidateToCheck - currentMultiple) / prime) * prime)
            if (currentMultiple < candidateToCheck) {
                currentMultiple += prime
            }
            return currentMultiple == candidateToCheck
        }
        override fun toString() = "$prime ($currentMultiple)"
    }

    /**
     * Implementation of sieve of Eratosthenes, with the following optimizations:
     * * The candidate primes to check are limited to the [sqrt] of [maxNum]
     *   * all values between [sqrt] ([maxNum]) and [maxNum] are either eliminated or primes
     * * The multiplier starts with the candidate value, not with 2
     *   * this is granted by the calculation of the [Prime.currentMultiple]
     * * Even numbers except 2 are excluded beforehand
     *   * this is transparent for this method, it does not impact the method
     */
    fun sift(): Sequence<Int> {
        try {
            candidates.forEach candidateLoop@ { candidate ->
                val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
                primes
                    .asSequence()
                    .forEach { prime ->
                    if (prime isDivisorOf candidate) {
                        // not a prime
                        return@candidateLoop
                    } else if (prime.prime > maxPrimeToTry) {
                        primes.add(Prime(candidate))
                        return@candidateLoop
                    }
                }
                primes.add(Prime(candidate))
            }
        }
        finally {
            print("After primes calculated: ")
            printMemUsage()
//            println("last 5 primes: ${primes.map { it.prime }.filterIndexed { index, prime -> index >= primes.size-6 }}")
        }
        return primes.asSequence().map { it.prime }
    }
}

fun main() {
    println(">>> Calculated multipliers, noloop")
    Eratosthenes(100).sift().forEach { println(it) }

    println("... Calculated multipliers, noloop")
    var startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(1_000_000).sift().count()}")
    var endTime = System.currentTimeMillis()
    println("Calculated multipliers - elapsed ms: ${endTime-startTime}")

    println("... Calculated multipliers, noloop")
    startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(100_000_000).sift().count()}")
    endTime = System.currentTimeMillis()
    println("Calculated multipliers, noloop - elapsed ms: ${endTime-startTime}")

    println("... Calculated multipliers, noloop")
    startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(300_000_000).sift().count()}")
    endTime = System.currentTimeMillis()
    println("Calculated multipliers, noloop - elapsed ms: ${endTime-startTime}")

    println("### Calculated multipliers, noloop ###")
}