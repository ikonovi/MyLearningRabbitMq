package rabbitmq._6_rpc;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RpcClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);
    public static final String REQUEST_QUEUE_NAME = RpcServer.RPC_QUEUE_NAME;
    private final Connection connection;
    private final Channel channel;

    public RpcClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672); // not over SSL
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        try (RpcClient rpcClient = new RpcClient()) {
            int originOfMaxFibNum = 32;
            for (int i = 0; i < originOfMaxFibNum; i++) {
                String iStr = Integer.toString(i);
                log.info(" [x] Requesting fib({})", iStr);
                String response = rpcClient.call(iStr);
                log.info(" [.] Got '{}'", response);
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            Thread.currentThread().interrupt(); // re-interrupted method
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException {
        final String correlationId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(correlationId).replyTo(replyQueueName).build();
        channel.basicPublish("", REQUEST_QUEUE_NAME, props, message.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> responseStringsQueue = new ArrayBlockingQueue<>(1);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties deliveryProperties = delivery.getProperties();
            if (deliveryProperties.getCorrelationId().equals(correlationId)) {
                String responseString = new String(delivery.getBody(), StandardCharsets.UTF_8);
                responseStringsQueue.offer(responseString);
            }
        };
        String consumeTag = channel.basicConsume(replyQueueName, true, deliverCallback, consumerTag -> {});
        String responseMessage = responseStringsQueue.take();
        channel.basicCancel(consumeTag);
        return responseMessage;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
