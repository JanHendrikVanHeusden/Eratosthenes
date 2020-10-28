## Eratosthenes & other approaches to prime finding algorithms

Reading in one of my old study books I stumbled across the well-known [sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes) algorithm.
As I had some spare time to study, I decided to see how I could implement this and other prime finding algorithms, and apply different approaches to these.

#### Contents
* [Eratosthenes & other approaches to prime finding algorithms](#eratosthenes--other-approaches-to-prime-finding-algorithms)
  * [Characteristics](#characteristics)
    * [1. Sieve of Eratosthenes](#1-sieve-of-eratosthenes)
    * [2. Naive approach: Try divide](#2-naive-approach-try-divide)
  * [Global optimizations](#global-optimizations)
    * [Further optimizations?](#further-optimizations)
  * [No (unit) tests](#no-unit-tests)
  * [Comparing approaches](#comparing-approaches)
    * [Details](#details)
    * [Memory usage](#memory-usage)
  * [Implemented approaches](#implemented-approaches)
    * [1. Sieve of Eratosthenes](#sieve-of-eratosthenes-implementations)
      * [The classic Eratosthenes approach](#the-classic-eratosthenes-approach)
      * [Two variations of the classic Eratosthenes approach](#two-variations-of-the-classic-eratosthenes-approach)
    * [2. Try divide](#try-divide-implementations)
      * [Just that, the naive approach](#just-that-the-naive-approach)
      * [Same, but keeping previous primes in memory as denominators](#same-but-keeping-previous-primes-in-memory-as-denominators)
      * [Parallel streams](#parallel-streams)
      * [RxJava](#rxjava)
      * [Kotlin coroutines using Flow](#kotlin-coroutines-using-flow)
      * [Kotlin coroutines using Channel](#kotlin-coroutines-using-channel)

### Characteristics
#### 1. Sieve of Eratosthenes
See https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes.
* A characteristic of the sieve of Eratosthenes is that it is not scalable to really high numbers (because you have to keep all previous primes in memory)
* The basic algorithm is simple and straightforward

#### 2. Naive approach: Try divide
In this approach, you just try to divide a number by all possible lower numbers, and if no one has a remainder of zero, you found a prime number
* Compared to the Eratosthenes sieve, there is no need to keep previous results in memory
* The basic algorithm is even more simple and straightforward 

### Global optimizations
There are some global optizations that are applied to all approaches:
* Skipping even numbers above 2
* Limiting the denominators to the square root of the candidate value

#### Further optimizations?
Several further optimizations are possible, but not applied:
* Many described optimizations try to stochastically determine whether a number is **not** a prime and exclude these.
* Other optimizations are more like determing the chance if something is (not) a prime
* In the naive *try divide* algorithms a preparing step could be added to find the first primes up to, say, 1000, and use these as denominators instead of just "any" number in this range; higher denominators would just follow the baise *try divide* pattern

### No (unit) tests
No unit tests or other tests are added. Each class file has a runnable `main` method.
The goal was just to see what approaches and optimizations could be used with different techniques; not to create a mature production & life cycle hardened project.

### Comparing approaches
All implementated approaches produce all primes from 2 up to a given max (say, 100M).
For the *sieve of Eratosthenes* this is in fact the only possible approach, as you can't find a higher prime value until you found all previous ones.

To allow comparison, I used the same approach for the "*try divide*" algorithms.
This approach is fair if you wanted to find all primes from 2 up to a given number anyway; but a bit unfair in other use cases, e.g. "find all primes between 1000000 and 1000100" or just "is this number a prime?"; these use cases are not really usable with the *sieve of Eratosthenes* approach, but would fit well to the "*try divide*" approach.

In other words, the naive "*try divide*" algorithm is much more versatile, but for comparison it is pushed into the harness dictated by the "*sieve of Eratosthenes*" algoritm.

#### Details
Furhter details of the results can be found in the kdoc header of each class file.

#### Memory usage
* For the non-reactive approaches, the memory usage is determined statically during execution, and is output to the terminal.
* This does not work well in reactive approaches (RxJava & coroutines). The same figures are output, but they do not reflect the real memory usage during execution. For this, external tooling should be used (e.g. Visual VM; to do yet)

### Implemented approaches
1. #### Sieve of Eratosthenes (implementations)
   1. ###### The classic Eratosthenes approach
      * Computationally really cheap, so by far the fastest (primes up to 100M in less than 5s on my laptop)
      * Not scalable for seriously high numbers (say, above 250M), mainly because of memory usage
         * On my laptop / JVM it runs out of memory when finding primes higher than ~ 300M
      * Not suitable to determine if just a single given number is a prime
   2. ###### Two variations of the classic Eratosthenes approach
      * Optimized for using less memory (say, -30%)
      * Quite a bit slower than the classic approach (~ 7 times slower)
      * Still not scalable for seriously high numbers

2. #### Try divide (implementations)
   1. ###### Just that, the naive approach
      * A lot slower than Eratosthenes
          * and much more so for higher numbers; say 60 to 100 times slower
      * No need to keep "previous" primes in memory, so usable for any number
      * Usable for any number up to the language limit (say, any `Int` or `Long`)
   2. ###### Same, but keeping previous primes in memory as denominators
      * A bit slower(!) than the "simple" naive approach (which uses all lower numbers as denominators), so what was meant as optimization rather appeared a change for worse.
        * Apperently iterating over and adding to the in-memory `List` takes more computation resources than the simple "just anything" naive approach
   3. ###### Parallel streams
      * No success, **much much** slower than the naive approach, and consumes all available CPU resources
         * Some typical optimizations (early jumping out of a loop) are not possible within the parallel stream application
   4. ###### RxJava
      * Naive approach combined with RxJava
      * Not any faster than the naive approach
      * But uses much less memory as results are not kept in memory but emitted on the fly
   5. ###### Kotlin coroutines using `Flow`
      * Naive approach combined with coroutines / `Flow`
      * Comparable (speed, memory) with the RxJava solution
   6. ###### Kotlin coroutines using `Channel`
      * Naive approach combined with coroutines / `Channel`
      * Speed comparable with the RxJava and coroutines / `Flow` solution
      * High memory consumption when using unlimited channel capacity (`Channel.UNLIMITED`) !!
         * On my laptop / JVM it runs out of memory after 20 minutes or so when finding primes up to about 300M; so even more memory usage as the classic *sieve of Eratosthenes* approach.
         * Switching to other capacity settings than `Channel.UNLIMITED` drops the speed by a factor 100 or 1000, which makes these nearly unusable for this use case. Might be worthwhile to further investigation why this is the case...? (to do)
