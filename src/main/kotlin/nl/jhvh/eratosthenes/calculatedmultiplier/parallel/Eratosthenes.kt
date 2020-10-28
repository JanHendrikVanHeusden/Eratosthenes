package nl.jhvh.eratosthenes.calculatedmultiplier.parallel

import nl.jhvh.eratosthenes.calculatedmultiplier.parallel.Eratosthenes.Prime
import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * An implementation that basically follows the classic sieve of Eratosthenes algorithm
 * (see [nl.jhvh.eratosthenes.classic.Eratosthenes]), but instead of holding all values in memory, the prime's
 * multiplier is calculated (and its current value remembered as [Prime.currentMultiple]).
 * > And, in this class, parallelism is added...
 * * But the effect is really counterproductive, producing 500k primes takes more time with this approach
 *   than producing 100M primes without parallelism...! And as expected, it consumes 100 % of all your CPU cores...
 *   (much more so than without parallelism)
 * * See comments of [sift] for more detail.
 *
 * > The candidate values are all integers in the range `2..`[maxNum], inclusive.
 * @param maxNum The end value of the range of candidate values
 */
class Eratosthenes(val maxNum: Int) {
    init {
        println("Primes up to: $maxNum")
        print("Start: ")
        printMemUsage()
    }

    val primes: MutableList<Prime> = mutableListOf(Prime(2))
    val candidates: Iterable<Int> = 3..maxNum step 2 // upfront optimization: skipping the even numbers

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    class Prime(val prime: Int) {

        var currentMultiple: Int = prime

        infix fun isDivisorOf(candidateToCheck: Int): Boolean {
            while (currentMultiple < candidateToCheck) {
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
     *
     * This method processes the sifting in a parallel way, in order to get more speed.
     * But the effect is contrary, it goes way slower...!
     * * One of the reasons for this is that in the parallelized loop it can't jump out of it
     *   (the compiler does not allow to `break` or `return@candidateLoop`), so even it was already determined
     *   that the candidate is a prime, it still has to fully complete the inner loop :-(
     */
    fun sift(): Sequence<Int> {
        try {
            candidates.forEach candidateLoop@ { candidate ->
                val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
                var maybePrime = true
                primes.parallelStream().filter { it.prime <= maxPrimeToTry}.forEach { prime ->
                    if (maybePrime) {
                        if (prime isDivisorOf candidate) {
                            maybePrime = false
                        }
                    }
                }
                if (maybePrime) {
                    primes.add(Prime(candidate))
                }
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
    println(">>> Calculated multipliers - parallel")
    Eratosthenes(100).sift().forEach { println(it) }
    println("... Calculated multipliers - parallel")
    val startTime = System.currentTimeMillis()
    println("# Primes: ${Eratosthenes(500_000).sift().count()}")
    val endTime = System.currentTimeMillis()
    println("Calculated multipliers - elapsed ms: ${endTime-startTime}")
    println("### Calculated multipliers - parallel ###")
}