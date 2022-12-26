package com.example.project_reactor_error_handling;


import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

public class AllErrorHandling {


    //OnError
    @Test
    public void testErrorFlowFlux() {
        Flux<Integer> fluxFromJust = Flux.just(1, 2, 3, 4, 5)
                .concatWith(Flux.error(new RuntimeException("Error")))
                .concatWith(Flux.just(6));

        fluxFromJust.subscribe(
                (it) -> System.out.println("Number is " + it),
                (e) -> e.printStackTrace(),
                () -> System.out.println("subscriber complete")
        );
        StepVerifier.create(fluxFromJust)
                .expectNext(1, 2, 3, 4, 5)
                .expectError(RuntimeException.class);

    }

    //onErrorResume
    @Test
    public void testOnErrorResume() throws InterruptedException {
        Flux<Integer> fluxFromJust = Flux.just(1, 2, 3, 4, 5)
                .concatWith(Flux.error(new RuntimeException("Test")))
                .concatWith(Flux.just(6)).onErrorResume(e -> {
                    System.out.println("Exception occured " + e.getMessage());
                    return Flux.just(7, 8);
                });

        StepVerifier.create(fluxFromJust)
                .expectNext(1, 2, 3, 4, 5)
                .expectNext(7, 8)
                .verifyComplete();

    }

    //on error Continue
    @Test
    public void testOnErrorContinue() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3,4,5)
                .map(i -> mapSomeValue(i))
                .onErrorContinue((e,i) -> {
                    System.out.println("Error for item " + i);
                });

        StepVerifier.create(fluxFromJust)
                .expectNext(1,2,4,5)
                .verifyComplete();

    }

    private int mapSomeValue(Integer i) {
        if(i ==3)
            throw new RuntimeException("Exception from map");
            return i;

    }

    //on Error Map
/*
    @Test
    public void testOnErrormap() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3,4,5)
                .concatWith(Flux.error(new RuntimeException("Test")))
                .concatWith(Flux.just(6))
                .map(i -> i*2)
                .onErrorMap(e -> new CustomException(e));
        StepVerifier.create(fluxFromJust)
                .expectNext(2,4,6,8,10)
                .expectError(CustomException.class);
    }
*/


    //************************************Retry************************
 /*   @Test
    public void testOnRetry() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3)
                .map(i -> i*2)
                .onErrorMap( e -> new CustomeException(e))
                .retry(2);
        StepVerifier.create(fluxFromJust)
                .expectNext(2,4,6)
                .expectNext(2,4,6)
                .expectNext(2,4,6)
                .expectError(CustomeException.class)
                .verify();


    }*/


    //**************************onErrorStop*******************
    @Test
    public void testErrorStop() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3)
                .concatWith(Flux.error(new RuntimeException("Test")))
                .concatWith(Flux.just(6))
                .map( i -> doubleValue(i))
                .onErrorStop();

        StepVerifier.create(fluxFromJust)
                .expectNext(2,4,6)
                .verifyError();

    }

    private Integer doubleValue(Integer i) {
        System.out.println("Doing multiple");
        return i*2;
    }


    //***********************doOnError*********************
    @Test
    public void testDoOnerror() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3,4,5)
                .concatWith(Flux.error(new RuntimeException("Test")))
                .doOnError(e -> System.out.println("Run some side effect"));

        StepVerifier.create(fluxFromJust)
                .expectNext(1,2,3,4,5)
                .expectError()
                .verify();
        TimeUnit.SECONDS.sleep(2);


    }


    //***********************doFinally***********************
    @Test
    public void testDoFinally() throws InterruptedException{
        Flux<Integer> fluxFromJust = Flux.just(1,2,3,4,5)
                .concatWith(Flux.error(new RuntimeException("Test")))
                .doFinally(i -> {
                    if (SignalType.ON_ERROR.equals(i)){
                        System.out.print("Completed with error");

                    }if(SignalType.ON_COMPLETE.equals(i)){
                        System.out.println("Completed without error");
                    }
                });
        StepVerifier.create(fluxFromJust)
                .expectNext(1,2,3,4,5)
                .expectError()
                .verify();
        TimeUnit.SECONDS.sleep(2);
    }


}