package rabbitmq._2_workqueues;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Worker {
    public static String QUEUE_NAME = "task_queue";

    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.receive();
    }

    public void receive() {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            // Do not close connection.
            Connection conn = factory.newConnection("subscriber's  connection");

            Channel channel = conn.createChannel();
            channel.basicQos(1); // accept only one unack-ed message at a time

            Map<String, Object> args = new HashMap<String, Object>();
                args.put("x-max-length", 20);
            channel.queueDeclare(
                    QUEUE_NAME,
                    true,
                    false,
                    false,
                    args);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


/*
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    channel.basicAck(deliveryTag, true);
                }
            };
            channel.basicConsume(MyQueue.task_queue.name(),false, defaultConsumer);
*/


            DeliverCallback deliverCallback = (String consumerTag, Delivery delivery) -> {
                byte[] bytes = delivery.getBody();
                String message = new String(bytes, StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");

                try {
                    doWork(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                    System.out.println(delivery);
                    System.out.println(" [x] Done");
                }
            };
            CancelCallback cancelCallback = (String consumerTag) -> {};
            // register consumer (subscription)
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.') {
                TimeUnit.SECONDS.sleep(1);
            }
        }
    }
}
