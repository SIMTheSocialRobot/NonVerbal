package edu.uw.hcde.capstone.nonverbal;

import javax.bluetooth.UUID;

import java.io.IOException;
import java.util.Enumeration;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.hcde.capstone.nonverbal.bluetooth.ServiceRecordAttribute;

public class BTDiscoveryListener implements DiscoveryListener {

	static final Logger logger = LoggerFactory.getLogger(BTDiscoveryListener.class);
	public static int numSearchThreads;
	
	static final UUID[] BT_SERVICES = new UUID[] {
		new UUID((long) 0x0001), // SDP
		new UUID((long) 0x0003), // RFCOMM
		new UUID((long) 0x0008), // OBEX
		new UUID((long) 0x000C), // HTTP
		new UUID((long) 0x0100), // L2CAP
		new UUID((long) 0x1101), // Serial
		new UUID(NonVerbalMQ.properties.getProperty("nonverbal.bt.service"), false)
	};
	
	static final UUID BT_SERVICE_UUID = new UUID(NonVerbalMQ.properties.getProperty("nonverbal.bt.service"), false);
	
	Object lock = new Object();
	NonVerbalMQ app;
	DiscoveryAgent agent;
	
	public BTDiscoveryListener(NonVerbalMQ app, DiscoveryAgent agent) {
		this.app = app;
		this.agent = agent;
		numSearchThreads++;
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		String name;
		LocalDevice self;

		try {
		    name = btDevice.getFriendlyName(false);
		}
		catch (Exception e) {
		    name = btDevice.getBluetoothAddress();
		}

		logger.info(String.format("Found device %s (%s)", name, btDevice.getBluetoothAddress()));
        
		try {
			self = LocalDevice.getLocalDevice();
			DiscoveryAgent agent = self.getDiscoveryAgent();
			agent.searchServices(new int[] {}, new UUID[] { BTDiscoveryListener.BT_SERVICE_UUID }, btDevice, this);
		}
		catch (BluetoothStateException e) {
			logger.error(String.format("Failed service discovery"), e);
		}
		
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		logger.info(String.format("Found %d services", servRecord.length));
		for (ServiceRecord service : servRecord) {
			Enumeration classIDs = (Enumeration) service.getAttributeValue(ServiceRecordAttribute.ServiceClassIDList.getValue()).getValue();
			do {
				String serviceUUID = ((UUID) ((DataElement) classIDs.nextElement()).getValue()).toString();
				
				if (serviceUUID.equals(NonVerbalMQ.properties.getProperty("nonverbal.bt.service"))) {
					logger.info(String.format("Found service %s!", serviceUUID));
					String url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
					try {
						app.openBTConnection(url);
					}
					catch (ClassCastException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			while(classIDs.hasMoreElements());
		}
		
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		String reason;
		boolean quit = true;
		numSearchThreads--;
		switch (respCode) {
			case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
				reason = "Terminated by application"; break;
			case DiscoveryListener.SERVICE_SEARCH_ERROR:
				reason = "Terminated by error"; break;
			case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
				reason = "No services found"; break;
			case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
				reason = "Device not reachable"; break;
			case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
				reason = "Complete"; quit = false; break;
			default:
				reason = "Unknown";
		}
		
		logger.info(String.format("Service search completed: %s.", reason));
		
		if (quit && numSearchThreads <= 0) {
			logger.info("Terminating program");
			System.exit(respCode);
		}
	}

	@Override
	public void inquiryCompleted(int discType) {
		synchronized(lock){
            lock.notify();
        }
	}

}
