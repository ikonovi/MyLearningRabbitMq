package rabbitmq._4_routing;

import java.util.Random;

public enum LogItemSeverity {
    info, error, warning;

    public static String getRandom() {
        LogItemSeverity[] values = LogItemSeverity.values();
        Random random = new Random();
        int randomIndex = random.nextInt(values.length);
        return values[randomIndex].name();
    }
}
