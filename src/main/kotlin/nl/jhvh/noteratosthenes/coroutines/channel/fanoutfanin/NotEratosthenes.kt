package nl.jhvh.noteratosthenes.coroutines.channel.fanoutfanin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.jhvh.util.printMemUsage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Implements a try divide algorithm to determine prime numbers by dividing a candidate by all preceding numbers
 * and check if the remainder is 0; if so, it's not a prime; it does so using Kotlin coroutines with
 * fan out / fan in patterns, inspired by [https://kotlinlang.org/docs/reference/coroutines/channels.html].
 *
 * > Pro:
 * * Basically simple algorithm
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * * modest memory consumption
 * * about 4 times faster than the more simplistic (non-reactive, no coroutines) or more sophisticated (coroutines, RxJava)
 *   implementations of the try divide algorithm
 * > Con:
 * * Code much more complicated / less readable
 * * Uses 100 % of the available CPU resources
 * * Closing the channel are non-intuitive (due to the nature of the use case), and hamper code cohesion (more coupling
 *   inside the classes' methods). The same goes (in less extent) for exception propagation (by default, exceptions are suppressed).
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
    val maxCoroutines = 10_000
    val candidateCount = AtomicInteger(0)
    val candidatePrimeCheckCount = AtomicInteger(0)

    init {
        print("After allocating candidates: ")
        printMemUsage()
    }

    suspend fun sendIfPrime(candidate: Int, channelOut: SendChannel<Int>) {
        val maxPrimeToTry = sqrt(candidate.toDouble()).toInt()
        var maybePrime = true
        for (denominator in (2..maxPrimeToTry)) {
            if (candidate % denominator == 0) {
                maybePrime = false
                break
            }
        }
        if (maybePrime) {
            channelOut.send(candidate)
        }
        candidatePrimeCheckCount.incrementAndGet()
    }

    fun CoroutineScope.launchPrimeChecker(intProducer: Iterator<Int>, channelOut: SendChannel<Int>): Job {
        val primeCheckerJob = launch {
            try {
                var candidate: Int = 0
                try {
                    while (true) {
                        // makes no sense to first check if iterator.hasNext() is true,
                        // other threads may call next() concurrently.
                        // So just call iterator.next() and catch the exception
                        synchronized(intProducer) {
                            // iterator.next() is not thread safe !
                            candidate = intProducer.next()
                        }
                        candidateCount.incrementAndGet()
                        sendIfPrime(candidate, channelOut)
                    }
                } catch (nsee: NoSuchElementException) {
                    // iterator has no values anymore -> completed successfully, so ignore
                }
            } catch (e: Exception) {
                val cancellationException = CancellationException("Cancelled due to ${e.javaClass.simpleName}: ${e.message}", e)
                this.coroutineContext.cancel(cancellationException)
                channelOut.close(cancellationException)
            }
        }
        return primeCheckerJob
    }

    /**
     * Determines prime numbers by dividing a candidate by all preceding numbers, with the following optimizations:
     * * The candidate primes to check are limited to the [sqrt] of [maxNum]
     *   * all values between [sqrt] ([maxNum]) and [maxNum] are either eliminated or primes
     * * Even numbers except 2 are excluded beforehand
     *   * this is transparent for this method, it does not impact the method
     */
    @ExperimentalCoroutinesApi
    fun primes(): Channel<Int> {
        val primeChannel = Channel<Int>()
        val coroutineCount = min(max(sqrt(maxNum.toDouble()).toInt(), 1), maxCoroutines)
        println("# of coroutines = $coroutineCount")
        val candidateIterator = candidates.iterator()
        val jobs: MutableList<Job> = mutableListOf()
        val closeChannelWhenComplete: () -> Unit = {
            if (candidatePrimeCheckCount.get() == candidateCount.get() && !candidateIterator.hasNext()) {
//                println("candidatePrimeCheckCount: $candidatePrimeCheckCount")
//                println("candidateCount: $candidateCount")
                val closeResult = primeChannel.close() // true if the Channel was open, false if already closed
//                println("Closing, was it open? $closeResult")
            }
        }
        GlobalScope.launch {
            for (coroutineNr in 1..coroutineCount) {
//                println("Launching prime checker #$coroutineNr")
                val job = launchPrimeChecker(candidateIterator, primeChannel)
                job.invokeOnCompletion { closeChannelWhenComplete.invoke() }
                jobs.add(job)
            }
        }
        return primeChannel
    }
}

@ExperimentalCoroutinesApi
fun main() {
    println(">>> Try divide - coroutines fan out / fan in (not Eratosthenes)")

    runBlocking {
        for (prime in NotEratosthenes(100).primes()) {
            println(prime)
        }
    }

    suspend fun retrievePrimeCount(maxCandidate: Int): Int {
        var primeCount = 0
        val startTime = System.currentTimeMillis()
        for (prime in NotEratosthenes(maxCandidate).primes()) {
            primeCount++
        }
        println("# Primes: $primeCount")
        val endTime = System.currentTimeMillis()
        println("Try divide - coroutines fan out / fan in (not Eratosthenes) - elapsed ms: ${endTime - startTime}")
        return primeCount
    }

    runBlocking {
        println("... Try divide - coroutines fan out / fan in (not Eratosthenes)")
        val primeCount = retrievePrimeCount(1_000_000)
        check(primeCount == 78498) {
            "Wrong value $primeCount for prime count, should be 78498!!"
        }
    }

    runBlocking {
        println("... Try divide - coroutines fan out / fan in (not Eratosthenes)")
        val primeCount = retrievePrimeCount(10_000_000)
        check(primeCount == 664579) {
            "Wrong value $primeCount for prime count, should be 664579!!"
        }
    }

    runBlocking {
        println("... Try divide - coroutines fan out / fan in (not Eratosthenes)")
        val primeCount = retrievePrimeCount(100_000_000)
        check(primeCount == 5761455) {
            "Wrong value $primeCount for prime count, should be 5761455!!"
        }
    }

    runBlocking {
        println("... Try divide - coroutines fan out / fan in (not Eratosthenes)")
        val primeCount = retrievePrimeCount(300_000_000)
        check(primeCount == 16252325) {
            "Wrong value $primeCount for prime count, should be 16252325!!"
        }
    }

    runBlocking {
        println("... Try divide - coroutines fan out / fan in (not Eratosthenes)")
        val primeCount = retrievePrimeCount(1_000_000_000)
    }

    println("### Try divide - coroutines fan out / fan in (not Eratosthenes) ###")
}


