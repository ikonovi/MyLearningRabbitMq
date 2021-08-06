package rabbitmq._5_topics;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import rabbitmq.enums.LogItemFacility;
import rabbitmq.enums.LogItemSeverity;

import java.nio.charset.StandardCharsets;

public class EmitLogTopic {
    public static final String EXCHANGE_NAME = "tut5.topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC.getType());

            String routingKey = generateRoutingKey();
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

    /**
     * @return words "<facility>.<severity>"
     */
    private static String generateRoutingKey() {
        return LogItemFacility.getRandom() + "." + LogItemSeverity.getRandom();
    }

    private static String getMessage() {
        return "Application log message";
    }
}
