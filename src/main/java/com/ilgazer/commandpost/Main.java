package com.ilgazer.commandpost;

import com.ilgazer.phonepilot.Axis;
import com.ilgazer.phonepilot.Mode;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);
        CompositeDisposable waveDisp = new CompositeDisposable();
        while (!server.isClosed() && scanner.hasNext()) {
            switch (scanner.next()) {
                case "mode":
                    switch (scanner.next()) {
                        case "get":
                            server.getMode()
                                    .subscribe(mode -> System.out.printf("Mode:%s%n", mode.toString()));
                            break;
                        case "set":
                            Utils.getEnum(scanner.next(), Mode::valueOf)
                                    .ifPresentOrElse(server::setMode, () -> System.out.println("Invalid mode."));
                            break;
                    }
                    break;
                case "axis":
                    String command = scanner.next();
                    Utils.getEnum(scanner.next(), Axis::valueOf).ifPresentOrElse(axis -> {
                        switch (command) {
                            case "set":
                                server.setAxis(axis, scanner.nextDouble());
                                break;
                            case "get":
                                server.getAxis(axis)
                                        .subscribe(val -> System.out.printf("Axis is set to:%f%n", val));
                                break;
                            case "wave":
                                waveDisp.add(Observable.interval(1, TimeUnit.MILLISECONDS)
                                        .map(count -> Math.abs(count % 4001 - 2000) - 1000)
                                        .map(i -> i / 1000.0)
                                        .observeOn(Schedulers.single())
                                        .flatMapCompletable(d -> server.setAxis(axis, d))
                                        .subscribe());
                                break;
                            case "unwave":
                                waveDisp.clear();
                                break;
                        }
                    }, () -> System.out.println("Invalid axis."));
                    break;

                case "exit":
                    server.close();
                    break;

                case "ping":
                    server.getLatency().subscribe(System.out::println);
            }
        }
    }

}
