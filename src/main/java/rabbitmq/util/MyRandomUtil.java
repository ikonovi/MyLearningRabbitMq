package rabbitmq.util;

import java.util.Random;

public class MyRandomUtil {
    private static final Random random = new Random();

    /**
     * Get random int number in range of from 0 to bound exclusive
     * @param bound end of range exclusive
     * @return
     */
    public static int randomArrayElementIndex(int bound) {
        return random.nextInt(bound);
    }

}
