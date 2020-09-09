package com.ilgazer.commandpost;

import java.util.Optional;
import java.util.function.Function;

public class Utils {
    public static <E extends Enum<E>> Optional<E> getEnum(String s, Function<String, E> valueOf) {
        try {
            return Optional.of(valueOf.apply(s.toUpperCase()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
