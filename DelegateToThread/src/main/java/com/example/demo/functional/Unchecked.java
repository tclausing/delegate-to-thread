package com.example.demo.functional;

import java.util.function.Function;

public class Unchecked {

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public static <T, R, E extends Exception> Function<T, R> unchecked(Unchecked.ThrowingFunction<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}