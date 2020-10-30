package nl.jhvh.noteratosthenes.primesinmemory

import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt


/**
 * Implements a simple algorithm to determine prime numbers by dividing a candidate by preceding (lower) prime numbers
 * and check if the remainder is 0; if so, it's not a prime.
 *
 * > Pro:
 * * simple algorithm
 * * not too high memory consumption, only the previous primes are kept in memory (max allocated heap memory ~ 3.5 MB for
 *   processing 1M candidates, 16 MB for 10M candidates, plus ~ 3.8 MB non-heap memory)
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * > Con:
 * * like with the Eratosthenes algorithm, you can only find a higher prime by determining all preceding primes down to 2 as well
 * * it is slow...! It takes more time to find 1M primes than it takes for [nl.jhvh.eratosthenes.classic.Eratosthenes] to find 100M primes...!
 *   * In fact, it is even a lot SLOWER than the naive try divide approach in [nl.jhvh.noteratosthenes.naive.NotEratosthenes],
 *     it seems that storing the previous primes and iterating over it takes more time than just trying all values, prime or not
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
                val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
                primes.asSequence().filter { it <= maxPrimeToTry}.forEach { prime ->
                    if (candidate % prime == 0) {
                        return@candidateLoop
                    }
                }
                primes.add(candidate)
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
    println(">>> Try divide (not Eratosthenes), preceding primes in memory")
    NotEratosthenes(100).primes().forEach { println(it) }
    println("... Try divide (not Eratosthenes), preceding primes in memory")
    var startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(1_000_000).primes().count()}")
    var endTime = System.currentTimeMillis()
    println("Try divide (not Eratosthenes), preceding primes in memory - elapsed ms: ${endTime - startTime}")
    println("... Try divide (not Eratosthenes), preceding primes in memory")
    startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(10_000_000).primes().count()}")
    endTime = System.currentTimeMillis()
    println("Try divide (not Eratosthenes), preceding primes in memory - elapsed ms: ${endTime - startTime}")
    println("### Try divide (not Eratosthenes), preceding primes in memory ###")
}