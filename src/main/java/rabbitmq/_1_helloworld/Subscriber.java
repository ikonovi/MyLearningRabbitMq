package rabbitmq._1_helloworld;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Subscriber {
    public static String QUEUE_NAME = "hello";

    public static void main(String[] args) {
        Subscriber subscriber = new Subscriber();
        subscriber.receive();
    }

    public void receive() {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            // Do not close connection.
            Connection conn = factory.newConnection("subscriber's  connection");
            Channel channel = conn.createChannel();
            channel.queueDeclare(
                    QUEUE_NAME,
                    false,
                    false,
                    false,
                    null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


            DeliverCallback deliverCallback = (String consumerTag, Delivery delivery) -> {
                byte[] bytes = delivery.getBody();
                String message = new String(bytes, StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };
            CancelCallback cancelCallback = consumerTag -> {}; // XXX
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
