package com.airline.scheduler.config;

import com.airline.scheduler.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ configuration for consuming booking events.
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "airline.topic";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";

    @Bean
    public TopicExchange airlineExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue emailQueue() {
        log.info("Creating Queue: {}", EMAIL_QUEUE);
        Queue queue = QueueBuilder.durable(EMAIL_QUEUE).build();
        log.info("✅ Queue '{}' created successfully", EMAIL_QUEUE);
        return queue;
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
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Map the flight-service class to scheduler-service class
        idClassMapping.put("com.airline.flight.event.BookingCreatedEvent", BookingCreatedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("com.airline.*");
        return classMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // Add callback for confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message confirmed sent to RabbitMQ");
            } else {
                log.error("Message failed to send to RabbitMQ. Cause: {}", cause);
            }
        });
        
        // Add callback for returns
        template.setReturnsCallback(returned -> {
            log.error("Message returned from RabbitMQ: {}", returned.toString());
        });
        
        log.info("RabbitTemplate configured with message converter");
        return template;
    }

    /**
     * Configure RabbitListenerContainerFactory for @RabbitListener annotations.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setAutoStartup(true);
        
        // Error handler configuration
        FatalExceptionStrategy strategy = new FatalExceptionStrategy() {
            @Override
            public boolean isFatal(Throwable t) {
                log.error("RabbitMQ listener fatal exception: {}", t.getMessage(), t);
                return false; // Don't stop listening on errors
            }
        };
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(strategy));
        
        log.info("✅ RabbitListenerContainerFactory configured");
        log.info("   Concurrent Consumers: 3-10");
        log.info("   Prefetch Count: 10");
        log.info("   Message Converter: Jackson2JsonMessageConverter");
        return factory;
    }

    /**
     * Verify RabbitMQ connection on startup.
     */
    @Bean
    public CommandLineRunner rabbitMqConnectionVerifier(ConnectionFactory connectionFactory) {
        return args -> {
            log.info("========================================");
            log.info("VERIFYING RABBITMQ CONNECTION");
            log.info("========================================");
            try {
                var connection = connectionFactory.createConnection();
                if (connection != null && connection.isOpen()) {
                    log.info("✅ RabbitMQ connection successful!");
                    log.info("   Host: {}", connectionFactory.getHost());
                    log.info("   Port: {}", connectionFactory.getPort());
                    connection.close();
                } else {
                    log.error("❌ RabbitMQ connection failed!");
                }
            } catch (Exception e) {
                log.error("❌ Failed to connect to RabbitMQ: {}", e.getMessage(), e);
                log.error("Please check:");
                log.error("  1. RabbitMQ is running");
                log.error("  2. Connection settings in application.yml");
                log.error("  3. Network connectivity");
            }
            log.info("========================================");
        };
    }
}
