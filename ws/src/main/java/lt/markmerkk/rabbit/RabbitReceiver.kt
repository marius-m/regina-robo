package lt.markmerkk.rabbit

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class RabbitReceiver {

    fun receiveMessage(message: String) {
        try {
            l.info("Handleing message: <$message>")
            Thread.sleep(3000L)
        } catch (e: InterruptedException) {
            l.warn("Interrupted!")
        }
    }

    companion object {
        private val l = LoggerFactory.getLogger(RabbitReceiver::class.java.simpleName)!!
    }

}