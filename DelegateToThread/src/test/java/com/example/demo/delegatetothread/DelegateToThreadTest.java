package com.example.demo.delegatetothread;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Configuration
@ComponentScan(basePackageClasses = DelegateToThread.class)
@EnableAsync
@EnableAspectJAutoProxy
public class DelegateToThreadTest {
    
    static int a, b;

    @Component
    static class Foo {

        @DelegateToThread("A-thread")
        public void a() {
            a++;
            assertThat(Thread.currentThread().getName(), equalTo("A-thread"));
            sleep(10);
        }

        @DelegateToThread("B-thread")
        public void b() {
            b++;
            assertThat(Thread.currentThread().getName(), equalTo("B-thread"));
            sleep(10);
        }

        @DelegateToThread("negative-thread")
        public void negative() {
            Assert.fail("negative");
        }
    }

    @Component
    static class Bar {

        @Autowired
        Foo foo;

        @Async
        CompletableFuture<Void> a() {
            foo.a();
            return CompletableFuture.completedFuture(null);
        }

        @Async
        CompletableFuture<Void> b() {
            foo.b();
            return CompletableFuture.completedFuture(null);
        }

        @Async
        CompletableFuture<Void> negative() {
            foo.negative();
            return CompletableFuture.completedFuture(null);
        }
    }

    @Autowired
    DelegateThreadLifecycle lifecycle;

    @Autowired
    Bar bar;

    @Test
    public void test() {
        lifecycle.start();

        CompletableFuture.allOf(bar.a(), bar.a(), bar.b(), bar.b()).join();
        assertThat(a, equalTo(2));
        assertThat(b, equalTo(2));

        // queue empties out, go again

        CompletableFuture.allOf(bar.a(), bar.a(), bar.a()).join();
        assertThat(a, equalTo(5));

        lifecycle.stop();
    }

    @Test
    public void negative() {
        lifecycle.start();

        try {
            bar.negative().join();
            Assert.fail("expected exception");
        } catch (CompletionException e) {
            AssertionError ae = (AssertionError) e.getCause();
            assertThat(ae.getMessage(), equalTo("negative"));
        }

        lifecycle.stop();
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
