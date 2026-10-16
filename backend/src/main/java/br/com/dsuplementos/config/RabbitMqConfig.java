package br.com.dsuplementos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.messaging.orders.enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    TopicExchange pedidosExchange(@Value("${app.messaging.orders.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    Queue pedidosCriadosQueue(@Value("${app.messaging.orders.queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    Binding pedidosCriadosBinding(
            Queue pedidosCriadosQueue,
            TopicExchange pedidosExchange,
            @Value("${app.messaging.orders.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(pedidosCriadosQueue).to(pedidosExchange).with(routingKey);
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
