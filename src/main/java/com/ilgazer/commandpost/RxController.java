package com.ilgazer.commandpost;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import net.java.games.input.Component;
import net.java.games.input.Controller;

import java.util.concurrent.TimeUnit;

public class RxController {

    public static Observable<ComponentChangeEvent> change(Controller controller) {
        PublishSubject<ComponentChangeEvent> subject = PublishSubject.create();
        Component[] components = controller.getComponents();
        float[] oldValues = new float[components.length];
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(ignore -> {
                    controller.poll();
                    for (int i = 0; i < oldValues.length; i++) {
                        float currentValue = components[i].getPollData();
                        float change = currentValue - oldValues[i];
                        float treshold = components[i].getDeadZone();
                        if (treshold < change || treshold < -change) {
                            subject.onNext(new ComponentChangeEvent(components[i], currentValue, change));
                            oldValues[i] = currentValue;
                        }
                    }
                });
        return subject;
    }

    public static class ComponentChangeEvent {
        private final Component component;
        private final float currentValue;
        private final float change;

        private ComponentChangeEvent(Component component, float currentValue, float change) {
            this.component = component;
            this.currentValue = currentValue;
            this.change = change;
        }

        public Component getComponent() {
            return component;
        }

        public float getCurrentValue() {
            return currentValue;
        }

        public float getChange() {
            return change;
        }
    }
}
