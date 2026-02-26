package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class RabbitMqTopologyConfiguration {

  @Bean
  public TopicExchange liveQuizEventsExchange(LiveQuizMessagingProperties properties) {
    return new TopicExchange(properties.exchange(), true, false);
  }

  @Bean
  public Queue liveQuizEmailMainQueue(LiveQuizMessagingProperties properties) {
    LiveQuizMessagingProperties.Email email = properties.email();
    return new Queue(
      email.mainQueue(),
      true,
      false,
      false,
      Map.of(
        "x-dead-letter-exchange",
        properties.exchange(),
        "x-dead-letter-routing-key",
        email.failedRoutingKey()
      )
    );
  }

  @Bean
  public Queue liveQuizEmailRetryQueue(LiveQuizMessagingProperties properties) {
    LiveQuizMessagingProperties.Email email = properties.email();
    return new Queue(
      email.retryQueue(),
      true,
      false,
      false,
      Map.of(
        "x-dead-letter-exchange",
        properties.exchange(),
        "x-dead-letter-routing-key",
        email.requestedRoutingKey()
      )
    );
  }

  @Bean
  public Queue liveQuizEmailDlq(LiveQuizMessagingProperties properties) {
    return new Queue(properties.email().dlq(), true);
  }

  @Bean
  public Binding liveQuizEmailMainBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizEmailMainQueue,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizEmailMainQueue)
      .to(liveQuizEventsExchange)
      .with(properties.email().requestedRoutingKey());
  }

  @Bean
  public Binding liveQuizEmailRetryBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizEmailRetryQueue,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizEmailRetryQueue)
      .to(liveQuizEventsExchange)
      .with(properties.email().retryRoutingKey());
  }

  @Bean
  public Binding liveQuizEmailDlqBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizEmailDlq,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizEmailDlq)
      .to(liveQuizEventsExchange)
      .with(properties.email().failedRoutingKey());
  }

  @Bean
  public Queue liveQuizSubmissionMainQueue(LiveQuizMessagingProperties properties) {
    LiveQuizMessagingProperties.Submission submission = properties.submission();
    return new Queue(
      submission.mainQueue(),
      true,
      false,
      false,
      Map.of(
        "x-dead-letter-exchange",
        properties.exchange(),
        "x-dead-letter-routing-key",
        submission.failedRoutingKey()
      )
    );
  }

  @Bean
  public Queue liveQuizSubmissionRetryQueue(LiveQuizMessagingProperties properties) {
    LiveQuizMessagingProperties.Submission submission = properties.submission();
    return new Queue(
      submission.retryQueue(),
      true,
      false,
      false,
      Map.of(
        "x-dead-letter-exchange",
        properties.exchange(),
        "x-dead-letter-routing-key",
        submission.requestedRoutingKey()
      )
    );
  }

  @Bean
  public Queue liveQuizSubmissionDlq(LiveQuizMessagingProperties properties) {
    return new Queue(properties.submission().dlq(), true);
  }

  @Bean
  public Binding liveQuizSubmissionMainBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizSubmissionMainQueue,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizSubmissionMainQueue)
      .to(liveQuizEventsExchange)
      .with(properties.submission().requestedRoutingKey());
  }

  @Bean
  public Binding liveQuizSubmissionRetryBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizSubmissionRetryQueue,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizSubmissionRetryQueue)
      .to(liveQuizEventsExchange)
      .with(properties.submission().retryRoutingKey());
  }

  @Bean
  public Binding liveQuizSubmissionDlqBinding(
    TopicExchange liveQuizEventsExchange,
    Queue liveQuizSubmissionDlq,
    LiveQuizMessagingProperties properties
  ) {
    return BindingBuilder
      .bind(liveQuizSubmissionDlq)
      .to(liveQuizEventsExchange)
      .with(properties.submission().failedRoutingKey());
  }
}
