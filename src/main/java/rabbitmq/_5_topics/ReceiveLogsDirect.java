package rabbitmq._5_topics;

import com.rabbitmq.client.*;
import rabbitmq.enums.LogItemFacility;
import rabbitmq.enums.LogItemSeverity;
import rabbitmq.util.MyRandomUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

public class ReceiveLogsDirect {
    private static final String EXCHANGE_NAME = EmitLogTopic.EXCHANGE_NAME;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC.getType());

        String queueName = "tut5." + LocalTime.now().toSecondOfDay();
        channel.queueDeclare(queueName,
                false,
                true,
                true,
                null);

        String bindingKey = getBindingKey();
        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
        System.out.println(" [*] Waiting for messages for bindingKey " + bindingKey
                + ". To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    private static String getBindingKey() {
        String[] topicBindVariants = {
                "#",
                LogItemFacility.getRandom() + ".*",
                "*." + LogItemSeverity.getRandom(),
                LogItemFacility.getRandom() + "." + LogItemSeverity.getRandom()
        };
        int index = MyRandomUtil.randomArrayElementIndex(topicBindVariants.length);
        return topicBindVariants[index];
    }
}
