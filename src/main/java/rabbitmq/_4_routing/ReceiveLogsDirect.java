package rabbitmq._4_routing;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

public class ReceiveLogsDirect {
    private static final String EXCHANGE_NAME = "tut4.direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT.getType());

        String routingKey = LogItemSeverity.getRandom();
        String queueName = "tut4." + routingKey + LocalTime.now().toSecondOfDay();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

        System.out.println(" [*] Waiting for " + routingKey +" messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
