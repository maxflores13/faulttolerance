package com.threepillar.faulttolerance;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/tolerance")
public class Process {

    @GetMapping
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackMethod")
    public String callExternalService() {
        log.info("executing process...");
        doFail();
        return "something";
    }

    public void doFail() {
        var random = new Random();
        if (random.nextInt(10) != 7) {
            log.warn("process failed.");
            throw new RuntimeException("do something to make implementation failed");
        }
    }

   public String fallbackMethod(Throwable throwable) {
        return "alternative path";
   }

   public String retryAlternativeMethod(Throwable throwable){
        return "retry fallback message";
   }

    public CompletableFuture<String> timerAlternativeMethod(Throwable throwable){
        return CompletableFuture.supplyAsync(() -> "time limiter fallback message");
    }

   @GetMapping("retry")
   @Retry(name="retryPolicy", fallbackMethod = "retryAlternativeMethod")
   public String retry(){
       log.info("trying process...");
       doFail();
       return "completed";
   }

   @GetMapping("timer")
   @TimeLimiter(name = "timePolicy", fallbackMethod = "timerAlternativeMethod")
   public CompletableFuture<String> timer(){
        log.info("reaching service...");
        doSleep();
        return CompletableFuture.supplyAsync(() -> "do it");
   }

   public void doSleep(){
       try {
           Thread.sleep(5000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }

   @GetMapping("/bulk")
   @Bulkhead(name = "bulkheadpolicy")
   public String bulkheadtest(){
       try {
           Thread.sleep(5000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
        return "ocurrio concurrencia";
   }
}
