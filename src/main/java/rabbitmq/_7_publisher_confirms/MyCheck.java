package rabbitmq._7_publisher_confirms;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MyCheck {
    public static void main(String[] args) {
        ConcurrentNavigableMap<Long, String> map = new ConcurrentSkipListMap<>();
        for (int i = 1; i <= 10; i++) {
            map.put((long) i, Integer.toString(i));
        }
        print("orig map", formatElements(map));

        ConcurrentNavigableMap<Long, String> portionOfMap = map.headMap((long) 5, true);
        print("portion of map", formatElements(portionOfMap));

        // Check that portion of map is backed by original map.
        // So, changes in the sub map are reflected in the orig map, and vice-versa.
        portionOfMap.pollFirstEntry();
        print("first elem removed: portion of map", formatElements(portionOfMap));
        print("orig map", formatElements(map));
    }

    private static void print(String message, String string) {
        System.out.println(message + "\n" +  string);
    }

    private static String formatElements(ConcurrentNavigableMap<Long, String> navigableMap) {
        StringBuffer stringBuffer = new StringBuffer();
        navigableMap.forEach((Long k, String v) -> {
            String keyValue = String.format("(%d, %s), ", k, v);
            stringBuffer.append(keyValue);
        });
        return stringBuffer
                .replace(stringBuffer.lastIndexOf(","), stringBuffer.length(), ".")
                .append("\n")
                .toString();
    }
}
