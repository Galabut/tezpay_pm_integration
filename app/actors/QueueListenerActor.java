package actors;

import akka.actor.AbstractActor;
import akka.actor.IllegalActorStateException;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.typesafe.config.Config;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class QueueListenerActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(context().system(), this);
    Config conf;
    private static String host;
    private static String username;
    private static String password;
    private static String queue;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    Channel channel;

    protected Config getConfig(){
       return play.Play.application().configuration().underlying();
    }
    protected String getQueueName(){
        return getConfig().getString("rabbit.queue");
    }

    @Override
    public void receive(PartialFunction<Object, BoxedUnit> receive) throws IllegalActorStateException {
        super.receive(receive);
    }

    public void startListener() throws Exception {
        connect();
        log.info("Starting listening of the queue " + queue);
    }

    public void stopListening() throws Exception {
        if (channel != null)
            channel.close();
        log.info("Stopped listening of the queue " + queue);
    }

    public void closeConnection() throws Exception {
        if (connection.isOpen()) {
            connection.close();
        }
        log.info("Closed connection to the queue " + queue);
    }

    public void connect() throws IOException, TimeoutException {
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(getQueueName(), true, false, false, null);
        channel.basicQos(getBasicQos());
        channel.basicConsume(getQueueName(), false, getEventConsumer(channel));
    }

    public void tryReconnect() throws InterruptedException {
        log.info("Retry to reconnect " + getQueueName() + " ...");
        try {
            connect();
        } catch (Exception e) {
            log.error("Reconnection failed " + getQueueName() + " next try after 1 second ");
            Thread.sleep(1000);
            try {
                connect();
            } catch (Exception ex) {
                log.error("Reconnection failed " + getQueueName());
                MailSender.send("RMQ connection failed : " + ex.getMessage(), log);
            }
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        conf = getConfig();
        host = conf.getString("rabbit.host");
        username = conf.getString("rabbit.username");
        password = conf.getString("rabbit.password");
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        if (username != null)
            connectionFactory.setUsername(username);
        if (password != null)
            connectionFactory.setPassword(password);
    }

    public void noAskCmd(long tag) {
        try {
            channel.basicNack(tag, false, true);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void commitTag(long tag) {
        try {
            channel.basicAck(tag, false);
        } catch (IOException e) {
            log.error(e.getMessage(), e.getCause());
        }
    }

}
