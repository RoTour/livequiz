package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.StudentVerificationEmailSender;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class EmailDispatchQueueTestConfig {

  @Bean
  @Primary
  public FakeRabbitTemplate fakeRabbitTemplate() {
    return new FakeRabbitTemplate();
  }

  @Bean
  @Primary
  public FakeSesStudentVerificationEmailSender fakeSesStudentVerificationEmailSender() {
    return new FakeSesStudentVerificationEmailSender();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  static final class FakeRabbitTemplate extends RabbitTemplate {

    private final List<CapturedRabbitMessage> capturedMessages = new CopyOnWriteArrayList<>();

    FakeRabbitTemplate() {
      super(mock(ConnectionFactory.class));
    }

    @Override
    public void convertAndSend(
      String exchange,
      String routingKey,
      Object object,
      MessagePostProcessor messagePostProcessor
    ) {
      byte[] body = String.valueOf(object).getBytes(StandardCharsets.UTF_8);
      Message message = new Message(body, new MessageProperties());
      if (messagePostProcessor != null) {
        message = messagePostProcessor.postProcessMessage(message);
      }
      this.capturedMessages.add(new CapturedRabbitMessage(exchange, routingKey, object, message));
    }

    List<CapturedRabbitMessage> allMessages() {
      return new ArrayList<>(this.capturedMessages);
    }

    List<CapturedRabbitMessage> messagesForRoutingKey(String routingKey) {
      return this.capturedMessages
        .stream()
        .filter(captured -> captured.routingKey().equals(routingKey))
        .toList();
    }

    void clear() {
      this.capturedMessages.clear();
    }
  }

  record CapturedRabbitMessage(String exchange, String routingKey, Object payload, Message message) {
    String payloadAsString() {
      if (this.payload instanceof String text) {
        return text;
      }
      return new String(this.message.getBody(), StandardCharsets.UTF_8);
    }
  }

  static final class FakeSesStudentVerificationEmailSender
    implements StudentVerificationEmailSender {

    private final List<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    @Override
    public void sendVerificationEmail(
      String email,
      String token,
      String verificationUrl,
      Instant expiresAt
    ) {
      this.sentEmails.add(new SentEmail(email, token, verificationUrl, expiresAt, Instant.now()));
    }

    List<SentEmail> sentEmails() {
      return new ArrayList<>(this.sentEmails);
    }

    int countForUtcDay(LocalDate utcDay) {
      return (int) this.sentEmails
        .stream()
        .filter(sentEmail -> sentEmail.sentAt().atZone(ZoneOffset.UTC).toLocalDate().equals(utcDay))
        .count();
    }

    void clear() {
      this.sentEmails.clear();
    }
  }

  record SentEmail(
    String email,
    String token,
    String verificationUrl,
    Instant expiresAt,
    Instant sentAt
  ) {}
}
