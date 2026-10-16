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
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.messaging.orders.enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    TopicExchange pedidosExchange(@Value("${app.messaging.orders.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    Queue pedidosCriadosQueue(@Value("${app.messaging.orders.queue}") String queue,
            @Value("${app.messaging.orders.exchange}") String exchange) {
        return org.springframework.amqp.core.QueueBuilder.durable(queue)
                .deadLetterExchange(exchange + ".dead")
                .deadLetterRoutingKey("pedido.email.falhou").build();
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
    org.springframework.amqp.core.Declarables pedidosDeadLetter(
            @Value("${app.messaging.orders.exchange}") String exchange,
            @Value("${app.messaging.orders.queue}") String queue) {
        var deadExchange = new org.springframework.amqp.core.DirectExchange(exchange + ".dead");
        var deadQueue = new Queue(queue + ".dead", true);
        return new org.springframework.amqp.core.Declarables(deadExchange, deadQueue,
                BindingBuilder.bind(deadQueue).to(deadExchange).with("pedido.email.falhou"));
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
