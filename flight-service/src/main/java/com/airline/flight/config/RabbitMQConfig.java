package com.airline.flight.config;

import com.airline.flight.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ configuration for messaging.
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "airline.topic";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String MILES_QUEUE = "miles.queue";
    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";

    @Bean
    public TopicExchange airlineExchange() {
        log.info("Creating TopicExchange: {}", EXCHANGE_NAME);
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue emailQueue() {
        log.info("Creating Queue: {}", EMAIL_QUEUE);
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public Queue milesQueue() {
        log.info("Creating Queue: {}", MILES_QUEUE);
        return QueueBuilder.durable(MILES_QUEUE).build();
    }

    @Bean
    public Binding emailQueueBinding(Queue emailQueue, TopicExchange airlineExchange) {
        log.info("Binding Queue '{}' to Exchange '{}' with Routing Key '{}'", 
                EMAIL_QUEUE, EXCHANGE_NAME, BOOKING_CREATED_ROUTING_KEY);
        Binding binding = BindingBuilder
                .bind(emailQueue)
                .to(airlineExchange)
                .with(BOOKING_CREATED_ROUTING_KEY);
        log.info("✅ Email queue binding created successfully");
        return binding;
    }

    @Bean
    public Binding milesQueueBinding(Queue milesQueue, TopicExchange airlineExchange) {
        return BindingBuilder
                .bind(milesQueue)
                .to(airlineExchange)
                .with("miles.*");
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Map class name to type id for RabbitMQ messages
        idClassMapping.put("com.airline.flight.event.BookingCreatedEvent", BookingCreatedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("com.airline.*");
        log.info("ClassMapper configured with BookingCreatedEvent mapping");
        return classMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        log.info("Jackson2JsonMessageConverter configured with class mapper");
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // Enable publisher confirms
        template.setMandatory(true);
        
        // Add callback for confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ Message confirmed sent to RabbitMQ");
                if (correlationData != null) {
                    log.info("   Correlation ID: {}", correlationData.getId());
                }
            } else {
                log.error("❌ Message failed to send to RabbitMQ. Cause: {}", cause);
            }
        });
        
        // Add callback for returns (messages that couldn't be routed)
        template.setReturnsCallback(returned -> {
            log.error("❌ Message returned from RabbitMQ (unroutable):");
            log.error("   Exchange: {}", returned.getExchange());
            log.error("   Routing Key: {}", returned.getRoutingKey());
            log.error("   Reply Text: {}", returned.getReplyText());
            log.error("   Message: {}", returned.getMessage());
        });
        
        log.info("✅ RabbitTemplate configured with message converter and callbacks");
        return template;
    }
}
