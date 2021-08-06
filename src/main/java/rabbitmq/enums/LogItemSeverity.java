package rabbitmq.enums;

import rabbitmq.util.MyRandomUtil;

public enum LogItemSeverity {
    info, error, warning;

    public static String getRandom() {
        LogItemSeverity[] values = LogItemSeverity.values();
        int randomIndex = MyRandomUtil.randomArrayElementIndex(values.length);
        return values[randomIndex].name();
    }
}
