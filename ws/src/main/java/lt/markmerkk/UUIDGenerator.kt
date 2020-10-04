package lt.markmerkk

import java.util.*


class UUIDGenerator {
    fun generate(): String {
        return UUID.randomUUID()
                .toString()
    }
}