package nl.jhvh.util

import java.lang.management.ManagementFactory

fun printMemUsage() {
    System.gc()
    val memBean = ManagementFactory.getMemoryMXBean()
    val heap = memBean.heapMemoryUsage
    val nonHeap = memBean.nonHeapMemoryUsage
    println("Used heap memory: ${heap.used/1024}k; Used non-heap memory: ${nonHeap.used/1024}k")
}