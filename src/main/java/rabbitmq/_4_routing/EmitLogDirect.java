package rabbitmq._4_routing;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import rabbitmq.enums.LogItemSeverity;

import java.nio.charset.StandardCharsets;

public class EmitLogDirect {
    public static final String EXCHANGE_NAME = "tut4.direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT.getType());

            String routingKey = LogItemSeverity.getRandom();
            String message = getMessage();

            channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    null,
                    message.getBytes(StandardCharsets.UTF_8)
            );
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
        }
    }

    private static String getMessage() {
        return "My log message";
    }
}
