package edu.uw.hcde.capstone.nonverbal;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class NonVerbalMQ {

	public final static Properties properties;
	static Logger logger = LoggerFactory.getLogger(NonVerbalMQ.class);
	
	static {
		InputStream stream = NonVerbalMQ.class.getClassLoader().getResourceAsStream("nonverbal.properties");
		properties = new Properties();
		
		try {
			properties.load(stream);
			logger.debug("Loaded properties");
			logger.debug(properties.propertyNames().toString());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		NonVerbalMQ app = new NonVerbalMQ();
		
		try {
			app.openMQConnection();
			
			if (app.getPreviousDevices().length == 0) {
				app.discoverBTDevices();
			}
			else {
				for (RemoteDevice device : app.getPreviousDevices()) {
					if (device.getBluetoothAddress().equals(properties.getProperty("nonverbal.bt.device"))) {
						app.inquireBTServices(device);
					}
				}
			}
			
		}
		catch (ConnectException e) {
			logger.error(String.format("Failed to connect to %s", app.getHost()), e);
		}
		catch (TimeoutException e) {

		}
		catch (BluetoothStateException e) {
			
		}
		catch (IOException e) {

		}
	}
	
	public NonVerbalMQ() {
		
	}
	
	private StreamConnection btConnection;
	
	public void discoverBTDevices() throws BluetoothStateException {
		LocalDevice self = LocalDevice.getLocalDevice();
		DiscoveryAgent agent = self.getDiscoveryAgent();
		agent.startInquiry(DiscoveryAgent.GIAC, new BTDiscoveryListener(this, agent)); 
	}
	
	public RemoteDevice[] getPreviousDevices() throws BluetoothStateException {
		LocalDevice self = LocalDevice.getLocalDevice();
		DiscoveryAgent agent = self.getDiscoveryAgent();
		return agent.retrieveDevices(DiscoveryAgent.CACHED | DiscoveryAgent.PREKNOWN);
	}
	
	public void inquireBTServices(RemoteDevice device) throws BluetoothStateException {
		logger.info(String.format("Opening connection to device %s", device.getBluetoothAddress()));
		LocalDevice self = LocalDevice.getLocalDevice();
		DiscoveryAgent agent = self.getDiscoveryAgent();
		logger.info(String.format("Searching for services on device %s", device.getBluetoothAddress()));
		agent.searchServices(new int[] {}, new UUID[] { BTDiscoveryListener.BT_SERVICE_UUID }, device, new BTDiscoveryListener(this, agent));
	}
	
	public void openBTConnection(String url) throws IOException {
		logger.info(String.format("Attempting to connect to %s", url));
		btConnection = (StreamConnection) Connector.open(url);
		logger.info("Connection established!");
	}
	
	public void openMQConnection() throws IOException, TimeoutException {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(properties.getProperty(getHost()));
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare(getQueue(), false, false, false, null);
	    
	    channel.basicConsume(getQueue(), true, new MessageConsumer(channel, this));
	}
	
	public StreamConnection getBTConnection() {
		return btConnection;
	}
	
	public String getHost() {
		return properties.getProperty("nonverbal.queue.host");
	}
	
	public String getQueue() {
		return properties.getProperty("nonverbal.queue.name");
	}
}

