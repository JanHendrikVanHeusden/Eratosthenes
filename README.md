## Eratosthenes & other approaches to prime finding algorithms

<summary>

#### About this project
Reading in one of my old study books I stumbled across the well-known [sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes) algorithm.

As I had some spare time to study, I decided to see how I could implement this algorithm and other prime finding algorithms, and apply different approaches to these, among others using coroutines and RxJava.  

My goal was to see how they would behave and find out how they could be optimized (speed, memory, resources).
It was never meant to be a mature production ready or life cycle friendly project.

----
#### Clear winners
* For not too high numbers: the **classic Eratoshenes** algorithm
   * Its simplicity and speed are unbeatable
   * Not feasible for really high numbers (say, above 300M) due to the high memory consumption (`OutOfMemoryError`)
* For higher numbers: the try-divide implementation with concurrent coroutines
   * Fastest implementation for higher numbers
      * uses 100% CPU resources
      * still over 30 times slower than the classic Eratothenes algorithm
   * Low memory usage, able to crunch > 1G candidates without strain
   * Code much more complicated
      * less coherent, less intuitive, less maintainable

> * For techies, you may want to jump to the **[Implemented approaches](#implemented-approaches)**  
> * Or you may want to see **[Some conclusions](#some-conclusions)**  
> * Or click below on ► Details for the Table of Contents.

----

<details>

##### Table of Contents
* [Eratosthenes & other approaches to prime finding algorithms](#eratosthenes--other-approaches-to-prime-finding-algorithms)
  * [About this project](#about-this-project)
  * [Clear winners](#clear-winners)
  * [Algorithm characteristics](#algorithm-characteristics)
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
      * [RxJava with parallelism](#rxjava-with-parallelism)
      * [Kotlin coroutines using Flow](#kotlin-coroutines-using-flow)
      * [Kotlin coroutines using Channel](#kotlin-coroutines-using-channel)
      * [Kotlin concurrent coroutines using Channel with fan-out / fan-in](#kotlin-coroutines-using-channel)
  * [Some conclusions](#some-conclusions)
----
</details>

</summary>

### Algorithm characteristics
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

##### Further optimizations?
Several further optimizations are possible, but not applied:
* Many described optimizations try to stochastically determine whether a number is **not** a prime and exclude these.
* Other optimizations are more like determing the chance if something is (not) a prime
* In the naive *try divide* algorithms a preparing step could be added to find the first primes up to, say, 1000, and use these as denominators instead of just "any" number in this range; higher denominators would just follow the baise *try divide* pattern

### No (unit) tests
No unit tests or other tests are added. Each class file has a runnable `main` method.  
The goal was just to see what approaches and optimizations could be used with different techniques; not to create a mature production hardness & life cycle friendly project.

### Comparing approaches
All implementated approaches produce all primes from 2 up to a given max (say, 100M).
> For the *sieve of Eratosthenes* this is in fact the only possible approach, as you can't find a higher prime value until you found all previous ones.

To allow comparison, I used the same approach for the "*try divide*" algorithms.  
This approach is fair if you wanted to find all primes from 2 up to a given number anyway; but a bit unfair in other use cases, e.g. "find all primes between 1000000 and 1000100" or just "is this number a prime?"; these use cases are not really usable with the *sieve of Eratosthenes* approach, but would fit well to the "*try divide*" approach.

In other words, the naive "*try divide*" algorithm is more versatile and scalable (albeit slow), but for comparison it is pushed into the harness dictated by the "*sieve of Eratosthenes*" algoritm.

##### Details
Furhter details of the results can be found in the kdoc header of each class file.

#### Memory usage
* For the non-reactive approaches, the memory usage is determined statically during execution, and is output to the terminal.
* This does not work well in reactive approaches (RxJava & coroutines). The same figures are output, but they do not reflect the real memory usage during execution.  
   > ***To do yet***  
   > For this, external tooling should be used (e.g. Visual VM)

### Implemented approaches
1. #### Sieve of Eratosthenes (implementations)
   1. ###### The classic Eratosthenes approach
      * Computationally really cheap, so **by far the fastest** (primes up to 100M in less than 5s on my laptop)
      * Not scalable for seriously high numbers (say, above 250M), mainly because of memory usage
         * On my laptop / JVM it runs out of memory when finding primes higher than ~ 300M
      * Not very suitable to determine if just a single given number is a prime
   2. ###### Two variations of the classic Eratosthenes approach
      * Optimized for using less memory (say, -30%)
         * Still, not scalable for seriously high numbers
      * Quite a bit slower than the classic approach (~ 7 times slower)

2. #### Try divide (implementations)
   1. ###### Just that, the naive approach
      * A lot slower than Eratosthenes
          * and much more so for higher numbers; say 60 to 100 times slower
      * No need to keep "previous" primes in memory, so usable for any number
      * Usable for any number up to the language limit (say, any `Int` or `Long`)
   2. ###### Same, but keeping previous primes in memory as denominators
      * A bit slower(!) than the "simple" try divde approach (which uses all lower numbers as denominators), so what was meant as optimization rather appeared a change for worse.
        * Apperently iterating over and adding to the in-memory `List` takes more computation resources than the simple "just anything" naive try divide approach
   3. ###### Parallel streams
      * No success, **much much** slower than the naive try divide approach, and consumes all available CPU resources
         * Some typical optimizations (early jumping out of a loop) are not possible within the parallel stream application
   4. ###### RxJava
      * Try divide approach combined with RxJava
      * Not any faster than the naive try divide approach
      * But uses much less memory as results are not kept in memory but emitted on the fly
      * Slightly more complicated / less intuitive code than non-RxJava approach
      * So main benefit of using RxJava in this approach is low memory consumption compared to returning collection
   5. ###### RxJava with parallelism
      * Slightly SLOWER than the non-parallel RxJava implementation
         * Different degrees of parallelism (say, 2 to >16) did not make much difference
         * Apparently the needed coordination of multiple threads takes more time than the theoretical benefit of running on mulitple threads / cores can compensate for.
   6. ###### Kotlin coroutines using `Flow`
      * Try divide approach combined with coroutines / `Flow`
      * Comparable (speed, memory) with the RxJava solution
      * No more complicated than the non-coroutine approach; a bit simpler than with RxJava
      * So main benefit of using coroutines in this approach is low memory consumption compared to returning collection
   7. ###### Kotlin coroutines using `Channel`
      * Try divide approach combined with coroutines / `Channel`
      * Speed comparable with the RxJava and coroutines / `Flow` solution
      * High memory consumption when using unlimited channel capacity (`Channel.UNLIMITED`) !!
         * On my laptop / JVM it runs out of memory after ~ 20 minutes when finding primes up to about 300M; so even more memory usage as the classic *sieve of Eratosthenes* approach.
         * Switching to other capacity settings than `Channel.UNLIMITED` drops the speed by a factor 100 or 1000, which makes these nearly unusable for this use case.  
           > ***To do yet***   
           Might be worthwhile to further investigate why this is the case...? 
      * Anyhow, not a feasible approach, as it uses as many or more resources than anything else, without better performance
   8. ##### Kotlin concurrent coroutines using `Channel` with fan-out / fan-in
      > Fan out / fan in approach inspired by https://kotlinlang.org/docs/reference/coroutines/channels.html
      * The only approach to beat the naive try divide, about 4 times faster (on my 8-core laptop)
         * But still way slower than the classic Eratothenes algorithm (over 6.5 minute for 300M candidates, Eratosthenes algorithm does that in 12.3 s, so more than 30 times slower than the Eratosthenes sieve)
      * Low memory consumption
         * The only one to crunch 1G candidates in a somewhat feasible timespan (37 minutes)
            * Other approaches are either *way slower* or *crash* with `OutOfMemoryError` for anything above ~ 300M candidates.
      * At the downside, the code is much more complicated, less intuitive, harder to maintain
          * Architecturally: less coherent, more internal coupling
      
### Some conclusions
* Coroutines and RxJava reduce memory usage as primes can be produced and consumed concurrently.  
  > Part of this can also be achieved by using `Sequence` instead of `Collection`.
* Running RxJava with parallel option does not offer any benefit for this use case, neither in speed nor in memory, while consuming all CPU resources
* Concurrent coroutines (fan out / fan in approach) give a nice performance boost
   * but much more complicated / less coherent code
   * consumes all CPU resources
   * scalable, does not run out of memory on high counts
  > For not too high candidate counts, the classic sieve of Eratosthenes algorithm is still unbeatable
* Non-concurrent coroutines and non-parallel RxJava reduce memory usage, but do not improve speed
