/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.mqtt.inst.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.agent.mqtt.inst.internal.NorthboundBroker;
import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ConfigurationExtension.class)
@ExtendWith(ServiceExtension.class)
public class MQTTAgentTest {

    private final BlockingQueue<String> events = new ArrayBlockingQueue<String>(16);
    private static final String PORT="1884";
    /**
     * @throws Exception
     */
    @Test    	
   	@WithFactoryConfiguration(
   			factoryPid = NorthboundBroker.MQTT_AGENT_BROKER,
   			name = "ag1",
  			location = "?",
   			properties = {
   					@Property(key = "port", value = PORT),
   					@Property(key = "qos", value = "1")
   			}
   	)
   	@WithFactoryConfiguration(
   			factoryPid = MQTTServerImpl.CONFIGURATION_PID,
   			name = "ag1",
   			location = "?",
   			properties = {
   					@Property(key = "port", value = PORT),
   					@Property(key = "autostart", value = "true")
   			}
   	)
    public void mqttAgentsTest(@InjectService SliderSetterItf slider, 
    		@InjectService Core core) throws Throwable {
    	Thread.sleep(5000);
        try (MqttClient client = new MqttClient("tcp://127.0.0.1:"+PORT, 
        		UUID.randomUUID().toString(), new MemoryPersistence())) {        
	        client.setCallback(new MqttCallback() {
	
				@Override
				public void connectionLost(Throwable cause) {
					cause.printStackTrace();					
				}
	
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String m = ("["+topic+"]" + message);
					events.offer(m);
					System.out.println(m);
				}
	
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {

				}
	        });
	        client.connect();
	        client.subscribe("/slider/cursor/position");
		        
		    AnonymousSession session = core.getAnonymousSession();
		    Integer value = getSliderValue(session);
		    
		    int targetValue = value == 0 ? 5 : 0;
		    
	        slider.move(targetValue);
	        assertEquals(targetValue, getSliderValue(session));
	        assertEquals("[/slider/cursor/position]" + targetValue, events.poll(2, TimeUnit.SECONDS));
	        
	        slider.move(45);
	        assertEquals(45, getSliderValue(session));
	        assertEquals("[/slider/cursor/position]45", events.poll(2, TimeUnit.SECONDS));
	        
	        assertTrue(events.isEmpty());
	        client.disconnect(500);
        }
    }

	private Integer getSliderValue(AnonymousSession session) {
		GetResponse getResponse = session.get("slider", "cursor", "position", DataResource.VALUE);
	    return getResponse.getResponse(Integer.class, "value");
	}
}
