package lt.markmerkk.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import lt.markmerkk.Converter
import lt.markmerkk.entities.RequestInput
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
open class RBProcessing(
        @Autowired private val converter: Converter,
        @Autowired private val objectMapper: ObjectMapper,
        @Autowired private val rabbitTemplate: RabbitTemplate
) {

    /**
     * Gets input in form of
     *
     *   data class RoboRespProcess(
     *      val id: String,
     *      val text: String,
     *      val recordDurationMillis: Long,
     *      val resources: List<String>,
     *      val extra: Map<String, Any?>
     *   )
     *
     */
    @RabbitListener(queues = [RabbitConfig.queueNameConvert])
    fun receiveMessage(
            message: String,
            channel: Channel,
            @Header(AmqpHeaders.DELIVERY_TAG) tag: Long
    ) {
        try {
            val inputAsRequest: Map<String, Any?> = objectMapper.readValue(message, Map::class.java) as Map<String, Any?>
            val inputExtras: InputExtras = InputExtras.fromMap(inputAsRequest)
            if (inputExtras.isEmpty()) {
                l.warn("No 'text' found in '${inputAsRequest}'")
                return
            }
            l.info("Processing input")
            val convertResult = converter.processRun(
                    RequestInput(
                            inputText = inputExtras.text,
                            extraEntityId = inputExtras.extraEntityId,
                            extraTextId = inputExtras.extraTextId
                    ),
                    sanitizeInput(inputAsRequest)
            )
            channel.basicAck(tag, false)
            l.info("Complete! $convertResult")
            rabbitTemplate.convertAndSend(
                    RabbitConfig.exchangeName,
                    "rss.result.text",
                    objectMapper.writeValueAsString(convertResult)
            )
        } catch (e: IllegalStateException) {
            channel.basicNack(tag, false, false)
            l.warn("Error converting", e)
        }
    }

    fun sanitizeInput(map: Map<String, Any?>): Map<String, Any> {
        return map.filter { it.value != null }
                .map { it.key to it.value!! }
                .toMap()
    }

    companion object {
        private val l = LoggerFactory.getLogger(RBProcessing::class.java.simpleName)!!
    }

}

data class InputExtras(
        val text: String,
        val extraEntityId: String,
        val extraTextId: String
) {

    fun isEmpty(): Boolean = text.isEmpty()

    companion object {
        fun asEmpty(): InputExtras = InputExtras("", "", "")
        fun fromMap(extras: Map<String, Any?>): InputExtras {
            val extractExtras = InputExtras(
                    text = extras.extractOrEmpty("text"),
                    extraEntityId = extras.extractOrEmpty("entityId"),
                    extraTextId = extras.extractOrEmpty("textId")
            )
            if (extractExtras.isEmpty()) {
                return asEmpty()
            }
            return extractExtras
        }
    }
}

fun Map<String, Any?>.extractOrEmpty(key: String): String {
    if (!containsKey(key)) {
        return ""
    }
    val value: String = getValue(key).toString()
    return when {
        value != "null" -> value
        else -> ""
    }
}

