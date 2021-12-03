package lt.markmerkk.rabbit

import lt.markmerkk.BuildConfig
import lt.markmerkk.RabbitCreds
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableAutoConfiguration
open class RabbitConfig {

    @Autowired lateinit var rabbitCreds: RabbitCreds
    @Autowired lateinit var buildConfig: BuildConfig

    @Bean
    @Profile("dev")
    open fun connectionFactoryDev(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        //connectionFactory.setAddresses("10.0.1.150:5672")
        connectionFactory.setAddresses("${buildConfig.dockerHost}:5672")
        connectionFactory.username = "springtest"
        connectionFactory.setPassword("testspring")
        return connectionFactory
    }

    @Bean
    @Profile("prod")
    open fun connectionFactoryProd(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setAddresses("${buildConfig.dockerHost}:5672")
        connectionFactory.username = rabbitCreds.user
        connectionFactory.setPassword(rabbitCreds.pass)
        return connectionFactory
    }

    @Bean
    open fun queBindings(): Declarables {
        val args = mapOf<String, Any>("x-max-priority" to 10)
        val queue1 = Queue(queueNameConvert, false, false, false, args)
        //val queue2 = Queue(queueNameResult, false, false , false)
        val topicExchange = TopicExchange(exchangeName)
        return Declarables(
                queue1,
                topicExchange,
                BindingBuilder.bind(queue1).to(topicExchange).with("rss.convert.*")
        )
    }

    companion object {
        const val queueNameConvert = "rss-convert"
        const val queueNameResult = "rss-result"
        const val exchangeName = "rss-exchange"
        private val l = LoggerFactory.getLogger(RabbitConfig::class.java.simpleName)!!
    }

}