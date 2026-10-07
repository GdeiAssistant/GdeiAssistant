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
}
