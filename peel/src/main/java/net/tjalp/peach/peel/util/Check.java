package net.tjalp.peach.peel.util;

import java.text.MessageFormat;
import java.util.Objects;

public class Check {

    private Check() {

    }

    public static void notNull(Object object, String reason) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(reason);
        }
    }

    public static void notNull(Object object, String reason, Object... arguments) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(MessageFormat.format(reason, arguments));
        }
    }

    public static void argCondition(boolean condition, String reason) {
        if (condition) {
            throw new IllegalArgumentException(reason);
        }
    }

    public static void argCondition(boolean condition, String reason, Object... arguments) {
        if (condition) {
            throw new IllegalArgumentException(MessageFormat.format(reason, arguments));
        }
    }

    public static void fail(String reason) {
        throw new IllegalArgumentException(reason);
    }

    public static void stateCondition(boolean condition, String reason) {
        if (condition) {
            throw new IllegalStateException(reason);
        }
    }

    public static void stateCondition(boolean condition, String reason, Object... arguments) {
        if (condition) {
            throw new IllegalStateException(MessageFormat.format(reason, arguments));
        }
    }
}
