package rabbitmq.enums;

import rabbitmq.util.MyRandomUtil;

public enum LogItemFacility {
    auth, cron, kern;

    public static String getRandom() {
        LogItemFacility[] values = LogItemFacility.values();
        int randomIndex = MyRandomUtil.randomArrayElementIndex(values.length);
        return values[randomIndex].name();
    }
}
