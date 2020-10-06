package lt.markmerkk

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class TimeProvider(
        val clock: Clock,
        val zoneId: ZoneId
) {
    fun now(): LocalDateTime = LocalDateTime.now(clock)
}