package com.ilgazer.commandpost;

import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.TimeUnit;

public class TestMain {
    public static void main(String[] args) {
        Flowable.generate(() -> 0, (integer, emitter) -> {
            emitter.onNext(integer);
            return ++integer;
        })
                .throttleLatest(1000, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);
    }
}
