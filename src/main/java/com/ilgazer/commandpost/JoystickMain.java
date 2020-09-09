package com.ilgazer.commandpost;

import com.ilgazer.phonepilot.Axis;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JoystickMain {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        System.out.println("Select controller:");
        for (int i = 0; i < controllers.length; i++) {
            System.out.println(i + ":" + controllers[i].getName());
        }

        Controller controller = controllers[scanner.nextInt()];
        Observable<RxController.ComponentChangeEvent> movementObservable = RxController.change(controller);

        EnumMap<Axis, CompositeDisposable> axisAttachments = new EnumMap<>(Axis.class);
        for (Axis axis : Axis.values) {
            axisAttachments.put(axis, new CompositeDisposable());
        }

/*        posStream
                .sample(100, TimeUnit.MILLISECONDS)
                .doOnNext(System.out::println)
                .flatMapCompletable(d -> server.setAxis(Axis.ROLL, d))
                .subscribe(() -> System.out.println("Huh?"), Throwable::printStackTrace);
*/
        boolean running = true;
        while (running) {
            switch (scanner.next()) {
                case "attach":
                    int invert;
                    String axisStr = scanner.next();
                    if (axisStr.equals("inv")) {
                        invert = -1;
                        axisStr = scanner.next();
                    } else {
                        invert = 1;
                    }
                    Utils.getEnum(axisStr, Axis::valueOf)
                            .ifPresentOrElse(axis -> {
                                        System.out.println("Please move a control on your joystick.");
                                        movementObservable
                                                .filter(e -> e.getChange() > 0.1 || -0.1 > e.getChange())
                                                .timeout(5, TimeUnit.SECONDS)
                                                .onErrorComplete(throwable -> throwable instanceof TimeoutException)
                                                .firstElement()
                                                .map(RxController.ComponentChangeEvent::getComponent)
                                                .doOnSuccess(c -> System.out.println(c.getName()))
                                                .subscribe(component -> axisAttachments.get(axis).add(
                                                        movementObservable
                                                                .filter(componentChangeEvent ->
                                                                        componentChangeEvent.getComponent() == component)
                                                                .map(RxController.ComponentChangeEvent::getCurrentValue)
                                                                .map(f -> f * invert)
                                                                .flatMapCompletable(f -> server.setAxis(axis, f))
                                                                .subscribe(() -> System.out.println("Infinite movementObservable ended."),
                                                                        Throwable::printStackTrace)));
                                    },
                                    () -> System.out.println("Invalid axis."));
                    break;
                case "detach":
                    Utils.getEnum(scanner.next(), Axis::valueOf)
                            .ifPresentOrElse(
                                    axis -> axisAttachments.get(axis).clear(),
                                    () -> System.out.println("Invalid axis."));
                    break;
                case "exit":
                    running = false;
                    axisAttachments.values().forEach(CompositeDisposable::dispose);
                    server.close();
                    System.exit(0);
                    break;
            }
        }
    }
}
