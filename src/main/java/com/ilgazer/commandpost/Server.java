package com.ilgazer.commandpost;

import com.ilgazer.phonepilot.Axis;
import com.ilgazer.phonepilot.Mode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.ilgazer.phonepilot.RemoteCommand.*;

public class Server {
    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    Scheduler ioThread;

    public Server() throws IOException {
        ServerSocket server = new ServerSocket(9090);
        socket = server.accept();
        System.out.println("connected");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        ioThread = Schedulers.from(Executors.newSingleThreadExecutor());
    }

    public Completable setAxis(Axis axis, double i) {
        return wrap(() -> {
            out.write(SET_AXIS);
            out.writeInt(axis.ordinal());
            out.writeDouble(i);
        });
    }

    public Single<Double> getAxis(Axis axis) {
        return wrap(() -> {
            out.write(GET_AXIS);
            out.writeInt(axis.ordinal());
            return in.readDouble();
        });
    }

    public Single<Mode> getMode() {
        return wrap(() -> {
            out.write(GET_MODE);
            return Mode.values[in.readInt()];
        });
    }

    public Completable setMode(Mode mode) {
        return wrap(() -> {
            out.write(SET_MODE);
            out.writeInt(mode.ordinal());
        });
    }

    public Single<Double> getLatency() {
        return wrap(() -> {
            long totalLatency = 0;
            for (int i = 0; i < 100; i++) {
                totalLatency -= System.currentTimeMillis();
                out.write(GET_MODE);
                in.readInt();
                totalLatency += System.currentTimeMillis();
            }
            return totalLatency / 100.0;
        });
    }

    private <T> SingleSubject<T> wrap(Callable<T> callable) {
        SingleSubject<T> singleSubject = SingleSubject.create();
        Single.fromCallable(callable)
                .subscribeOn(ioThread)
                .subscribe(singleSubject);
        return singleSubject;
    }

    private CompletableSubject wrap(Action action) {
        CompletableSubject completableSubject = CompletableSubject.create();
        Completable.fromAction(action)
                .subscribeOn(ioThread)
                .subscribe(completableSubject);
        return completableSubject;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
