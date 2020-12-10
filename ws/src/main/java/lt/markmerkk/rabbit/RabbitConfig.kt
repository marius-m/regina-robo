package lt.markmerkk.rabbit

import org.apache.tomcat.jni.Buffer.address
import org.apache.tomcat.jni.User.username
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableAutoConfiguration
open class RabbitConfig {

    @Bean
    open fun queue(): Queue {
        return Queue(queueName, false)
    }

    @Bean
    open fun exchange(): TopicExchange {
        return TopicExchange(topicExchangeName)
    }

    @Bean
    open fun binding(
            queue: Queue,
            exchange: TopicExchange
    ): Binding {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("rss.*")
    }

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
    open fun container(
            connectionFactory: ConnectionFactory,
            listenerAdapter: MessageListenerAdapter
    ): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory
        container.setQueueNames(queueName)
        container.setMessageListener(listenerAdapter)
        container.setPrefetchCount(1)
        return container
    }

    @Bean
    open fun listenerAdapter(
            receiver: RabbitReceiver
    ): MessageListenerAdapter {
        return MessageListenerAdapter(receiver, "receiveMessage")
    }

    companion object {
        const val queueName = "rss-queue"
        const val topicExchangeName = "rss-exchange"
    }

}