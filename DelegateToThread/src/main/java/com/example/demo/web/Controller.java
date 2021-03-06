package com.example.demo.web;

import static com.example.demo.functional.Unchecked.unchecked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ParallelService;
import com.example.demo.service.RestrictedService;

@RestController()
@RequestMapping("/times6")
public class Controller {

    static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private ParallelService parallelService;
    
    @Autowired
    private RestrictedService restrictedService;

    @PostMapping
    public int put() throws ExecutionException {
        return restrictedService.workA() +
                restrictedService.workB() +
                Stream.of(parallelService.workA(), parallelService.workA(), parallelService.workB(), parallelService.workB())
                    .map(unchecked(CompletableFuture::get))
                    .reduce((a, b) -> a + b)
                    .get();
    }
}
