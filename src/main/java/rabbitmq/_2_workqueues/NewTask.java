package rabbitmq._2_workqueues;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class NewTask {
    public static String QUEUE_NAME = "task_queue";

    public static void main(String[] args) {
        NewTask newTask = new NewTask();
        newTask.publish(args);

    }

    public void publish(String[] argv) {
        ConnectionFactory factory = new ConnectionFactory();

        try (Connection connection = factory.newConnection("publicher's connection");
                Channel channel = connection.createChannel()) {

            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-max-length", 20);
            channel.queueDeclare(
                    QUEUE_NAME,
                    true,
                    false,
                    false,
                    args);

            String routingKey = QUEUE_NAME;
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .deliveryMode(2) // 2 - persistent
                    .build();
            //AMQP.BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN;
            String message = String.join(" ", argv);
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
