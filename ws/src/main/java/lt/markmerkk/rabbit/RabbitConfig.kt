package lt.markmerkk.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import lt.markmerkk.Converter
import lt.markmerkk.entities.RequestInput
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableAutoConfiguration
open class RabbitConfig {

    @Bean
    open fun connectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        //connectionFactory.setAddresses("10.0.1.150:5672")
        connectionFactory.setAddresses("10.0.1.150:5672")
        connectionFactory.username = "test"
        connectionFactory.setPassword("test")
        return connectionFactory
    }

    @Bean
    open fun queBindings(): Declarables {
        val queue1 = Queue(queueNameConvert, false)
        val queue2 = Queue(queueNameResult, false)
        val topicExchange = TopicExchange(exchangeName)
        return Declarables(
                queue1,
                queue2,
                topicExchange,
                BindingBuilder.bind(queue1).to(topicExchange).with("rss.convert.*"),
                BindingBuilder.bind(queue2).to(topicExchange).with("rss.result.*")
        )
    }

    companion object {
        const val queueNameConvert = "rss-convert"
        const val queueNameResult = "rss-result"
        const val exchangeName = "rss-exchange"
        private val l = LoggerFactory.getLogger(RabbitConfig::class.java.simpleName)!!
    }

}