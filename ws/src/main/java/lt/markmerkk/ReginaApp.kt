package lt.markmerkk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ReginaApp

fun main(args: Array<String>) {
    runApplication<ReginaApp>(*args)
}
