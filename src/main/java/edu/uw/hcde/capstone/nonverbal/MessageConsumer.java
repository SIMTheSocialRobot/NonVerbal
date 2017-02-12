package edu.uw.hcde.capstone.nonverbal;

import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MessageConsumer extends DefaultConsumer {

	static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
	
	NonVerbalMQ app;
	DataOutputStream btStream;
	
	public MessageConsumer(Channel channel, NonVerbalMQ app) {
		super(channel);
		this.app = app;
	}
	
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
		String message = new String(body, "UTF-8");
		logger.info(String.format("Received: %s", message));
		
		if (app.getBTConnection() != null) {
			if (btStream == null) {
				btStream = app.getBTConnection().openDataOutputStream();
			}
			
			btStream.write(body);
			logger.info(String.format("Sent message to %s", app.getBTConnection().toString()));
		}
	}

}
