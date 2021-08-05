package rabbitmq._1_helloworld;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Publisher {
    public static String QUEUE_NAME = "hello";

    public static void main(String[] args) {
        Publisher publisher = new Publisher();
        publisher.publish();

    }

    public void publish() {
        ConnectionFactory factory = new ConnectionFactory();

        try (Connection connection = factory.newConnection("publicher's connection");
                Channel channel = connection.createChannel()) {

            channel.queueDeclare(
                    QUEUE_NAME,
                    false,
                    false,
                    false,
                    null);


            String routingKey = QUEUE_NAME;
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .build();
            String message = "Hello World!";
            byte[] messageBytes = message.getBytes();
            channel.basicPublish(
                    "",
                    routingKey,
                    basicProperties,
                    messageBytes);
            System.out.println(" [x] Sent '" + message + "'");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
