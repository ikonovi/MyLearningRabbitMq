package rabbitmq.util;

public class Fibonacci {
    public static int calculate(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return calculate(n - 2) + calculate(n - 1);
    }
}
