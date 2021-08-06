package rabbitmq._3_publish_subscribe;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * Production [Non-]Suitability Disclaimer
 * topics such as connection management, error handling, connection recovery, concurrency
 * and metric collection are largely omitted
 */
public class ReceiveLogs {
    public static final String EXCHANGE_NAME = EmitLog.EXCHANGE_NAME;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT.getType());
        // declare a server-named exclusive, autodelete, non-durable queue
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
