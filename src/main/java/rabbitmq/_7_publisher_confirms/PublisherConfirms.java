package rabbitmq._7_publisher_confirms;

import com.rabbitmq.client.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class PublisherConfirms {

    static final int MESSAGE_COUNT = 25_000;

    static Connection createConnection() throws Exception {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost("localhost");
        cf.setUsername("guest");
        cf.setPassword("guest");
        return cf.newConnection();
    }

    public static void main(String[] args) throws Exception {
//        publishMessagesIndividually();
//        publishMessagesInBatch();
        handlePublishConfirmsAsynchronously();
    }

    /**
     * Strategy #1: Publishing Messages Individually
     *  We publish a message as usual and wait for its confirmation.
     *  It significantly slows down publishing, as the confirmation of a message blocks the publishing of
     * all subsequent messages.
     */
    static void publishMessagesIndividually() throws Exception {
        try (Connection connection = createConnection()) {
            Channel channel = connection.createChannel();
            String queueName = "tutorial7.Publishing-Messages-Individually";
            channel.queueDeclare(queueName, false, false, true, null);
            channel.confirmSelect(); // !!
            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                channel.basicPublish("", queueName, null, body.getBytes());
                channel.waitForConfirmsOrDie(5_000);
            }
            long end = System.nanoTime();
            long timeElapsed = Duration.ofNanos(end - start).toMillis();
            System.out.format("Published %,d messages individually in %,d ms%n", MESSAGE_COUNT, timeElapsed);
        }
    }

    /**
     * Strategy #2: Publishing Messages in Batches
     * faster in 4 times in this code
     */
    static void publishMessagesInBatch() throws Exception {
        try (Connection connection = createConnection()) {
            Channel channel = connection.createChannel();
            String queue = "tutorial7.Publish-Messages-In-Batch";
            channel.queueDeclare(queue, false, false, true, null);
            channel.confirmSelect(); // !!
            int batchSize = 100;
            int outstandingMessageCount = 0;

            Instant start = Instant.now();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                channel.basicPublish("", queue, null, body.getBytes());
                outstandingMessageCount++;

                if (outstandingMessageCount == batchSize) {
                    channel.waitForConfirmsOrDie(5_000);
                    outstandingMessageCount = 0;
                }
            }

            if (outstandingMessageCount > 0) {
                channel.waitForConfirmsOrDie(5_000);
            }
            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
            System.out.format("Published %,d messages in batch in %,d ms%n", MESSAGE_COUNT, timeElapsed);
        }
    }

    /**
     * Strategy #3: Handling Publisher Confirms Asynchronously
     */
    static void handlePublishConfirmsAsynchronously() throws Exception {
        try (Connection connection = createConnection()) {
            Channel channel = connection.createChannel();
            String queue = "tutorial7.Handling-Publisher-Confirms-Asynchronously";
            channel.queueDeclare(queue, false, false, true, null);
            channel.confirmSelect();

            // ожидающие подтверждения - номер сообщения, текст
            ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
            // очистить ожидающих подтверждения
            ConfirmCallback cleanOutstandingConfirmCallback = (long sequenceNumber, boolean multiple) -> {
                if (multiple) {
                    ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(
                            sequenceNumber, true
                    );
                    confirmed.clear(); // used fact that headMap is backed by original map. So, elements will be
                    // removed from there as well.
                } else {
                    outstandingConfirms.remove(sequenceNumber);
                }
            };
            ConfirmCallback  logErrorConfirmCallback = (long sequenceNumber, boolean multiple) -> {
                String body = outstandingConfirms.get(sequenceNumber);
                System.err.format("Message with body %s has been nack-ed. Sequence number: %d, multiple: %b%n",
                        body, sequenceNumber, multiple
                );
                cleanOutstandingConfirmCallback.handle(sequenceNumber, multiple);
            };
            channel.addConfirmListener(cleanOutstandingConfirmCallback,  logErrorConfirmCallback);

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                long nextPublishSeqNo = channel.getNextPublishSeqNo();
                outstandingConfirms.put(nextPublishSeqNo, body);
                channel.basicPublish("", queue, null, body.getBytes());
            }

            // Note, 2nd arg is the lambda expr that returns boolean value.
            BooleanSupplier noMessageWaitingConfirms = outstandingConfirms::isEmpty;
            if (!waitUntil(Duration.ofSeconds(60), noMessageWaitingConfirms) ) {
                throw new IllegalStateException("All messages could not be confirmed in 60 seconds");
            }

            long end = System.nanoTime();
            long elapsed = Duration.ofNanos(end - start).toMillis();
            System.out.format("Published %,d messages and handled confirms asynchronously in %,d ms%n",
                    MESSAGE_COUNT, elapsed);
        }
    }

    static boolean waitUntil(Duration timeout, BooleanSupplier condition) throws InterruptedException {
        int waitedMillis = 0;
        // Note, how to execute Supplier or get result of Supplier
        while (!condition.getAsBoolean() && waitedMillis < timeout.toMillis()) {
            TimeUnit.MILLISECONDS.sleep(100L);
            waitedMillis = +100;
        }
        return condition.getAsBoolean();
    }
}
