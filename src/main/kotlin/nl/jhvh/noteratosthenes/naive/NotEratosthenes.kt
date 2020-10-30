package nl.jhvh.noteratosthenes.naive

import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * Implements a naive approach of the try divide algorithm to determine prime numbers by dividing a candidate by all
 * preceding numbers and check if the remainder is 0; if so, it's not a prime
 *
 * > Pro:
 * * Very simple algorithm
 * * moderate memory consumption, only the previous primes are kept in memory
 *   (max allocated heap memory ~ 3.5 MB for 1M candidates, 16 MB for 10M candidates, 124 MB for 100M candidates,
 *   plus ~ 3.8 MB non-heap memory)
 * * it can easily be made stateless / reactive
 * > Con:
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * * it is not very fast...! It takes twice as much time to process 10M candidate values than it takes for [nl.jhvh.eratosthenes.classic.Eratosthenes]
 *   to process 100M candidates, and 60 times more to process 100M candidates...!
 *   Processing 100M candidates takes almost 5 minutes (compared to 5s for the classic Eratosthenes implementation).
 *   * But in fact, it is even a lot FASTER than the "optimized" approach in [nl.jhvh.noteratosthenes.primesinmemory.NotEratosthenes],
 *     it seems that just trying all values, prime or not, is faster than storing the previous primes and iterating over it.
 *     (and because of this, it can be made stateless)
 *
 * > The candidate values are all integers in the range `2..`[maxNum], inclusive.
 * @param maxNum The end value of the range of candidate values
 */
class NotEratosthenes(val maxNum: Int) {
    init {
        println("Primes up to: $maxNum")
        print("Start: ")
        printMemUsage()
    }

    private val primes: MutableList<Int> = mutableListOf(2)
    val candidates: Iterable<Int> = 3..maxNum step 2 // upfront optimization: skipping the even numbers

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    fun isPrime(candidate: Int): Boolean {
        val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
        (2..maxPrimeToTry).forEach { prime ->
            if (candidate % prime == 0) {
                return false
            }
        }
        return true
    }

    /**
     * Determines prime numbers by dividing a candidate by all preceding numbers, with the following optimizations:
     * * The candidate primes to check are limited to the [sqrt] of [maxNum]
     *   * all values between [sqrt] ([maxNum]) and [maxNum] are either eliminated or primes
     * * Even numbers except 2 are excluded beforehand
     *   * this is transparent for this method, it does not impact the method
     */
    fun primes(): Sequence<Int> {
        try {
            candidates.forEach candidateLoop@ { candidate ->
                if (isPrime(candidate)) {
                    primes.add(candidate)
                }
            }
        } finally {
            print("After primes calculated: ")
            printMemUsage()
//            println("last 5 primes: ${primes.filterIndexed { index, prime -> index >= primes.size-6 }}")
        }
        return primes.asSequence()
    }
}

fun main() {
    println(">>> Try divide naive (not Eratosthenes)")
    NotEratosthenes(100).primes().forEach { println(it) }
    println("... Try divide naive (not Eratosthenes)")
    var startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(1_000_000).primes().count()}")
    var endTime = System.currentTimeMillis()
    println("Try divide naive (not Eratosthenes) - elapsed ms: ${endTime - startTime}")
    println("... Try divide naive (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(10_000_000).primes().count()}")
    endTime = System.currentTimeMillis()
    println("Try divide naive (not Eratosthenes) - elapsed ms: ${endTime - startTime}")
    println("### Try divide naive (not Eratosthenes) ###")
}