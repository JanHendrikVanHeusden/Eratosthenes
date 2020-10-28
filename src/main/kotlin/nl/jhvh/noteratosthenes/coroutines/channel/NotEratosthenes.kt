package nl.jhvh.noteratosthenes.coroutines.channel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * Implements a naive algorithm to determine prime numbers by dividing a candidate by all preceding numbers
 * and check if the remainder is 0; if so, it's not a prime; it does so using Kotlin coroutines with [Channel]
 *
 * > Pro:
 * * Very simple algorithm
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * > Con:
 * * high memory consumption because of the unlimited channel, causes [OutOfMemoryError] when processing 300M candidates,
 *   so doing not really better there than the classic sieve of Eratosthenes algorithm
 *    * When we limit the [Channel] by stating any other value than the [Channel.UNLIMITED], the performance appears
 *      to be really disastrous somehow (???)
 * * not really faster than the more simplistic (non-reactive, no coroutines) implementations of the naive algorithm
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

    val candidates: Iterable<Int> =
        (1..maxNum step 2).asIterable().asSequence().map { if (it == 1) 2 else it }.asIterable()

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
    suspend fun primes(): Channel<Int> {
        val iterator = candidates.iterator()
        var candidateCount = 0
        var runCount = 0
        val primeChannel: Channel<Int> = Channel(capacity = Channel.UNLIMITED)
        coroutineScope {
            while (iterator.hasNext()) {
                candidateCount++
                val candidate = iterator.next()
                launch {
                    if (isPrime(candidate)) {
                        primeChannel.send(candidate)
                    }
                    runCount++
                    if (runCount == candidateCount) {
                        primeChannel.close()
                    }
                }
            }
        }
        return primeChannel
    }

}

fun main() {
    println(">>> Try divide - coroutines channel (not Eratosthenes)")
    runBlocking {
        for (x in NotEratosthenes(100).primes()) { println(x) }
    }

    println("... Try divide - coroutines channel (not Eratosthenes)")
    var startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(1_000_000).primes().consumeAsFlow().count() }")
    }
    var endTime = System.currentTimeMillis()
    println("Try divide - coroutines channel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - coroutines channel (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(10_000_000).primes().consumeAsFlow().count()}")
    }
    endTime = System.currentTimeMillis()
    println("Try divide - coroutines channel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - coroutines channel (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(100_000_000).primes().consumeAsFlow().count()}")
    }
    endTime = System.currentTimeMillis()
    println("Try divide - coroutines channel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("### Try divide - coroutines channel (not Eratosthenes) ###")
}