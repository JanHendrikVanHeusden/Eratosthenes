package nl.jhvh.noteratosthenes.coroutines.flow

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import nl.jhvh.util.printMemUsage
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Implements a naive algorithm to determine prime numbers by dividing a candidate by all preceding numbers
 * and check if the remainder is 0; if so, it's not a prime; it does so using Kotlin coroutines with [Flow]
 *
 * > Pro:
 * * Very simple algorithm
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * * modest memory consumption
 * > Con:
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

    val candidates: Iterable<Int> = (1..maxNum step 2).asIterable().asSequence().map { if (it == 1) 2 else it }.asIterable()

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    fun isPrime(candidate: Int): Boolean {
        val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
        (2..maxPrimeToTry).forEach { prime ->
            if (candidate % prime == 0) {
//                println("candidate $candidate is not a prime")
                return false
            }
        }
//        println("candidate $candidate IS a prime !!!!")
        return true
    }

    /**
     * Determines prime numbers by dividing a candidate by all preceding numbers, with the following optimizations:
     * * The candidate primes to check are limited to the [sqrt] of [maxNum]
     *   * all values between [sqrt] ([maxNum]) and [maxNum] are either eliminated or primes
     * * Even numbers except 2 are excluded beforehand
     *   * this is transparent for this method, it does not impact the method
     */
    fun primes(): Flow<Int> {
        val iterator = candidates.iterator()
        val finished = AtomicBoolean(false)
        return flow {
            while (!finished.get()) {
                try {
                    val candidate = iterator.next()
                    if (isPrime(candidate)) {
                        emit(candidate)
                    }
                } catch (e: NoSuchElementException) {
                    finished.set(true)
                }
            }
        }.buffer(min(maxNum/5, 10_000))
    }
}

fun main() {
    println(">>> Try divide - coroutines flow (not Eratosthenes)")
    runBlocking {
        NotEratosthenes(100).primes().collect { println(it) }
    }

    println("... Try divide - coroutines flow (not Eratosthenes)")
    var startTime = System.currentTimeMillis()
    runBlocking {
        val primes = NotEratosthenes(1_000_000).primes()
        println("# Primes: ${primes.count()}")
    }
    var endTime = System.currentTimeMillis()
    println("Try divide - coroutines flow (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - coroutines flow (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(10_000_000).primes().count()}")
    }
    endTime = System.currentTimeMillis()
    println("Try divide - coroutines flow (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - coroutines flow (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(100_000_000).primes().count()}")
    }
    endTime = System.currentTimeMillis()
    println("Try divide - coroutines flow (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - coroutines flow (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    runBlocking {
        println("# Primes: ${NotEratosthenes(300_000_000).primes().count()}")
    }
    endTime = System.currentTimeMillis()
    println("Try divide - coroutines flow (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("### Try divide - coroutines flow (not Eratosthenes) ###")
}