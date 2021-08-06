package rabbitmq._6_rpc;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbitmq.util.Fibonacci;

import java.nio.charset.StandardCharsets;

/**
 * Consumer
 */
public class RPCServer {
    private static final Logger log = LoggerFactory.getLogger(RPCServer.class);
    public static final String RPC_QUEUE_NAME = "tut6.rpc_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(
                    RPC_QUEUE_NAME,
                    false,
                    false,
                    false,
                    null);
            channel.queuePurge(RPC_QUEUE_NAME);

            // In order to spread the load equally over multiple servers
            channel.basicQos(1);

            log.info(" [x] Awaiting RPC requests");
            Object monitor = new Object();

            DeliverCallback deliverCallback = (String consumerTag, Delivery delivery) -> {

                String replyMessageFibNum = "";
                try {
                    String consumedMessageNum = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    int n = Integer.parseInt(consumedMessageNum);
                    log.info(" [.] fib({})", n);
                    replyMessageFibNum += Fibonacci.calculate(n);
                } finally {
                    String routingKey = delivery.getProperties().getReplyTo(); // reply_to queue name
                    String correlationId = delivery.getProperties().getCorrelationId();
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                            .correlationId(correlationId)
                            .build();

                    channel.basicPublish(
                            "",
                            routingKey,
                            replyProps,
                            replyMessageFibNum.getBytes(StandardCharsets.UTF_8));

                    long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);

                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {}));

            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}