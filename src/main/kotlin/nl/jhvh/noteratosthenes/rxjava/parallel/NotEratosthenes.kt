package nl.jhvh.noteratosthenes.rxjava.parallel

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.parallel.ParallelFlowable
import io.reactivex.rxjava3.schedulers.Schedulers
import nl.jhvh.util.printMemUsage
import kotlin.math.sqrt

/**
 * Implements a try divide algorithm to determine prime numbers by dividing a candidate by all preceding numbers
 * and check if the remainder is 0; if so, it's not a prime; it does so in a reactive way, using RxJava with parallelism.
 *
 * > Pro:
 * * Very simple algorithm
 * * really low memory consumption, no need to keep primes in memory (max allocated heap memory ~ 2 to 2.3 MB
 *   for any(!) number of candidates; and the non-heap memory consumption rising slightly from ~ 5.4 MB for 1M candidates
 *   to 6.3 MB for 100M; so really modest figures)
 * * can easily be made stateless / reactive
 * > Con:
 * * contrary to the Eratosthenes algorithm, you CAN find a prime without determining all preceding primes
 * * it is slow...! It takes more time to process 1M candidate values than it takes for [nl.jhvh.eratosthenes.classic.Eratosthenes]
 *   to process 100M candidates...!
 *   Processing 100M candidates takes over 5 minutes (compared to less than 5s for the classic Eratosthenes implementation),
 *   so over 60 times slower.
 *   * In fact, it is even somewhat SLOWER than the non-parallel execution of [nl.jhvh.noteratosthenes.rxjava.NotEratosthenes].
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
    fun primes(): ParallelFlowable<Int> {
        val flowable: Flowable<Int> = Flowable.create({ emitter ->
            val iterator = candidates.iterator()
            try {
                while (true) {
                    var candidate: Int
                    // makes no sense to first check if iterator.hasNext() is true,
                    // other threads may call next() concurrently.
                    // So just do iterator.next() and catch the exception
                    do {
                        synchronized(iterator) {
                            // iterator.next() is not thread safe !
                            candidate = iterator.next()
                        }
                    } while (!isPrime(candidate))
                    emitter.onNext(candidate)
                }
            } catch (nsee: NoSuchElementException) {
                // iterator has no values anymore -> completed successfully
                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)
        return flowable.parallel() // defaults to the CPU core count
    }
}

fun main() {
    println(">>> Try divide - RxJava, parallel (not Eratosthenes)")
    val scheduler = NotEratosthenes(100).primes().runOn(Schedulers.io())

    println("Parallelism: ${scheduler.parallelism()}")

    scheduler.sequential().forEach {println(it)}

    println("... Try divide - RxJava, parallel (not Eratosthenes)")
    var startTime = System.currentTimeMillis()
    println(NotEratosthenes(1_000_000).primes().runOn(Schedulers.io()).sequential().count().blockingGet() )
    var endTime = System.currentTimeMillis()
    println("Try divide - RxJava, parallel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - RxJava, parallel (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(10_000_000).primes().runOn(Schedulers.io()).sequential().count().blockingGet()}")
    endTime = System.currentTimeMillis()
    println("Try divide - RxJava, parallel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("... Try divide - RxJava, parallel (not Eratosthenes)")
    startTime = System.currentTimeMillis()
    println("# Primes: ${NotEratosthenes(100_000_000).primes().runOn(Schedulers.io()).sequential().count().blockingGet()}")
    endTime = System.currentTimeMillis()
    println("Try divide - RxJava, parallel (not Eratosthenes) - elapsed ms: ${endTime - startTime}")

    println("### Try divide - RxJava, parallel (not Eratosthenes) ###")
}