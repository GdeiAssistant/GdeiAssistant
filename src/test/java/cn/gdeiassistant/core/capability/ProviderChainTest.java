package cn.gdeiassistant.core.capability;

import cn.gdeiassistant.common.exception.*;
import org.junit.jupiter.api.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderChainTest {
    ExecutorService executor;
    @BeforeEach void setup() { executor = Executors.newFixedThreadPool(2); }
    @AfterEach void close() { executor.shutdownNow(); }
    ServiceProvider<String,String> provider(String name, int priority, String output) throws Exception {
        @SuppressWarnings("unchecked") ServiceProvider<String,String> p = mock(ServiceProvider.class);
        when(p.providerName()).thenReturn(name);when(p.priority()).thenReturn(priority);
        when(p.isConfigured()).thenReturn(true);when(p.isHealthy()).thenReturn(true);
        when(p.execute("input")).thenReturn(output);return p;
    }
    ProviderChain<String,String> chain(List<ServiceProvider<String,String>> providers, Duration budget) {
        return new ProviderChain<>("synthetic",providers,null,null,(input, output)->output!=null&&output.matches("[0-9]{4}"),executor,budget);
    }
    @Test void unusableOutputsFallThroughAndStatusDistinguishesUnverifiedFromSuccess() throws Exception {
        var empty=provider("empty",0,"");var wrong=provider("wrong",1,"not digits");var valid=provider("valid",2,"1234");
        var chain=chain(List.of(valid,wrong,empty),Duration.ofSeconds(1));
        assertEquals("not_verified",chain.getProviderStatus().get(0).get("lastOutcome"));
        assertEquals("1234",chain.execute("input"));
        verify(empty).execute("input");verify(wrong).execute("input");verify(valid).execute("input");
        assertEquals("failure",chain.getProviderStatus().get(0).get("lastOutcome"));
        assertEquals("success",chain.getProviderStatus().get(2).get("lastOutcome"));
    }
    @Test void deadlineStopsHungProviderAndNeverStartsAnotherAfterBudgetExpires() throws Exception {
        var hung=provider("hung",0,"0000");var fallback=provider("fallback",1,"1234");
        var entered=new CountDownLatch(1);var cancelled=new CountDownLatch(1);
        when(hung.execute("input")).thenAnswer(call->{entered.countDown();try{new CountDownLatch(1).await();}finally{cancelled.countDown();}return "0000";});
        long begin=System.nanoTime();
        assertThrows(ProviderChainExhaustedException.class,()->chain(List.of(hung,fallback),Duration.ofMillis(150)).execute("input"));
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-begin)<1500);
        assertTrue(entered.await(1,TimeUnit.SECONDS));assertTrue(cancelled.await(1,TimeUnit.SECONDS));verify(fallback,never()).execute("input");
    }
    @Test void rejectionIsReportedAsUnavailableAndInterruptionIsPreserved() throws Exception {
        var p=provider("valid",0,"1234");
        var rejected=new ProviderChain<String,String>("rejected",List.of(p),null,null,(input,output)->true,task->{throw new RejectedExecutionException();},Duration.ofSeconds(1));
        assertThrows(ProviderChainExhaustedException.class,()->rejected.execute("input"));verify(p,never()).execute("input");
        Thread.currentThread().interrupt();try{assertThrows(ProviderChainExhaustedException.class,()->chain(List.of(p),Duration.ofSeconds(1)).execute("input"));assertTrue(Thread.currentThread().isInterrupted());}finally{Thread.interrupted();}
    }
    @Test void openBreakerFallsBackThenProbesAndRecoversWithoutRestart() throws Exception {
        var primary=provider("primary",0,"1234");var fallback=provider("fallback",1,"5678");
        var registry=io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.ofDefaults();
        var time=new java.util.concurrent.atomic.AtomicReference<>(java.time.Instant.parse("2026-01-01T00:00:00Z"));
        var clock=new java.time.Clock() {
            @Override public java.time.ZoneId getZone(){return java.time.ZoneOffset.UTC;}
            @Override public java.time.Clock withZone(java.time.ZoneId zone){return this;}
            @Override public java.time.Instant instant(){return time.get();}
        };
        var config=io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom().clock(clock)
                .slidingWindowSize(2).minimumNumberOfCalls(2).failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30)).permittedNumberOfCallsInHalfOpenState(1)
                .automaticTransitionFromOpenToHalfOpenEnabled(false).build();
        var breaker=registry.circuitBreaker("synthetic.primary",config);
        when(primary.execute("input")).thenThrow(new ProviderException("synthetic outage"));
        var chain=new ProviderChain<String,String>("synthetic",List.of(primary,fallback),null,registry,
                (input,output)->output!=null,executor,Duration.ofSeconds(2));
        assertEquals("5678",chain.execute("input"));assertEquals("5678",chain.execute("input"));
        assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN,breaker.getState());
        doReturn("1234").when(primary).execute("input");
        assertEquals("5678",chain.execute("input"));verify(primary,times(2)).execute("input");
        time.updateAndGet(instant->instant.plusSeconds(31));
        assertEquals("1234",chain.execute("input"));
        assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED,breaker.getState());
        assertFalse(chain.isCompletelyDown());verify(primary,times(3)).execute("input");
    }

}
