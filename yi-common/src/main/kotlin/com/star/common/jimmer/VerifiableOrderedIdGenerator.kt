package com.star.common.jimmer

import org.babyfish.jimmer.sql.meta.UserIdGenerator
import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant

/**
 * 给 Users 主键生成可校验的有序 Long。
 *
 * 结果落在 JavaScript 安全整数内，按时间大致递增。组成是版本、相对秒、机器号、序号和 4 位校验。
 * 同一秒内序号用完会等到下一秒。时钟小幅回拨会短暂等待，回拨超过 5 毫秒则拒绝生成。
 * 机器号优先取网卡地址，读不到时随机。校验盐来自系统属性 ed.id.salt，没配则用内置默认值。
 */
class VerifiableOrderedIdGenerator : UserIdGenerator<Long> {

    private val workerId: Int
    private var lastEpochSecond: Long = -1L
    private var sequence: Int = 0

    constructor() {
        workerId = generateWorkerId()
    }

    constructor(workerId: Int) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw IllegalArgumentException(
                "Worker ID must be between 0 and $MAX_WORKER_ID, got: $workerId",
            )
        }
        this.workerId = workerId
    }

    override fun generate(entityType: Class<*>): Long = nextId()

    @Synchronized
    fun nextId(): Long {
        var currentSecond = Instant.now().epochSecond
        if (currentSecond < lastEpochSecond) {
            val offsetMs = (lastEpochSecond - currentSecond) * 1000L
            if (offsetMs <= CLOCK_BACKWARD_TOLERANCE_MS) {
                try {
                    Thread.sleep(offsetMs)
                    currentSecond = Instant.now().epochSecond
                } catch (exception: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw RuntimeException("Clock backward wait interrupted", exception)
                }
            } else {
                throw RuntimeException("Clock moved backwards by $offsetMs ms, refusing to generate VOID")
            }
        }

        if (currentSecond == lastEpochSecond) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0) {
                currentSecond = waitUntilNextSecond(lastEpochSecond)
            }
        } else {
            sequence = 0
        }

        lastEpochSecond = currentSecond
        return compose(currentSecond, workerId, sequence)
    }

    private fun waitUntilNextSecond(lastSecond: Long): Long {
        var second = Instant.now().epochSecond
        while (second <= lastSecond) {
            second = Instant.now().epochSecond
        }
        return second
    }

    private companion object {
        const val MAX_WORKER_ID: Int = (1 shl 7) - 1
        const val MAX_SEQUENCE: Int = (1 shl 9) - 1
        const val CLOCK_BACKWARD_TOLERANCE_MS: Long = 5L
        const val CURRENT_VERSION: Int = 1
        const val EPOCH_SECOND: Long = 1_704_067_200L
        const val TS_BITS = 29
        const val CHK_BITS = 4
        const val SEQ_SHIFT = CHK_BITS
        const val WORKER_SHIFT = SEQ_SHIFT + 9
        const val TS_SHIFT = WORKER_SHIFT + 7
        const val VER_SHIFT = TS_SHIFT + TS_BITS
        const val CHK_MASK = (1 shl CHK_BITS) - 1
        const val WORKER_MASK = (1 shl 7) - 1
        const val TS_MASK = (1L shl TS_BITS) - 1
        const val VER_MASK = (1 shl 3) - 1

        val checksumSalt: Long = resolveSalt()

        fun compose(epochSecond: Long, workerId: Int, sequence: Int): Long {
            val relativeSecond = epochSecond - EPOCH_SECOND
            if (relativeSecond < 0 || relativeSecond > TS_MASK) {
                throw IllegalStateException("VOID 时间戳超出可编码范围: $epochSecond")
            }
            val checksum = computeChecksum(relativeSecond, workerId, sequence)
            return ((CURRENT_VERSION and VER_MASK).toLong() shl VER_SHIFT) or
                ((relativeSecond and TS_MASK) shl TS_SHIFT) or
                ((workerId and WORKER_MASK).toLong() shl WORKER_SHIFT) or
                ((sequence and MAX_SEQUENCE).toLong() shl SEQ_SHIFT) or
                (checksum and CHK_MASK).toLong()
        }

        fun computeChecksum(relativeSecond: Long, workerId: Int, sequence: Int): Int {
            val mix = CURRENT_VERSION.toLong() * 0x9E3779B97F4A7C15UL.toLong() xor
                relativeSecond * 0xC6A4A7935BD1E995UL.toLong() xor
                workerId.toLong() * 0x165667B19E3779F9UL.toLong() xor
                sequence.toLong() * 0x85EBCA77C2B2AE63UL.toLong() xor
                checksumSalt
            return ((mix xor (mix ushr 32)) and CHK_MASK.toLong()).toInt()
        }

        fun resolveSalt(): Long {
            val configured = System.getProperty("ed.id.salt")
            if (!configured.isNullOrBlank()) {
                return try {
                    java.lang.Long.parseUnsignedLong(configured.trim())
                } catch (_: NumberFormatException) {
                    configured.trim().hashCode().toLong() and 0xFFFFFFFFL
                }
            }
            return 0x5A494544_2024L
        }

        fun generateWorkerId(): Int {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val hardwareAddress = interfaces.nextElement().hardwareAddress
                    if (hardwareAddress != null && hardwareAddress.size >= 6) {
                        val id = ((hardwareAddress[hardwareAddress.size - 2].toInt() and 0xFF) shl 8) or
                            (hardwareAddress[hardwareAddress.size - 1].toInt() and 0xFF)
                        return id and MAX_WORKER_ID
                    }
                }
            } catch (_: Exception) {
                // 读不到网卡地址时退回随机节点。
            }
            return SecureRandom().nextInt(MAX_WORKER_ID + 1)
        }
    }
}
