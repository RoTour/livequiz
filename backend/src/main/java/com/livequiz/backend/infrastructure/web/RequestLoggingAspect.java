package com.livequiz.backend.infrastructure.web;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class RequestLoggingAspect {

  public static final Logger LOGGER = LoggerFactory.getLogger(
    RequestLoggingAspect.class
  );

  @Around(
    "@annotation(com.livequiz.backend.infrastructure.web.LogExecutionTime)"
  )
  public Object logExecutionTime(ProceedingJoinPoint joinPoint)
    throws Throwable {
    String methodName = joinPoint.getSignature().toShortString();
    StopWatch stopWatch = new StopWatch();

    stopWatch.start();
    try {
      return joinPoint.proceed();
    } finally {
      stopWatch.stop();
      // Log as INFO. In high-traffic apps, consider DEBUG.
      LOGGER.info(
        "⏱️ Execution of '{}' took {} ms",
        methodName,
        stopWatch.getTotalTimeMillis()
      );
    }
  }
}
