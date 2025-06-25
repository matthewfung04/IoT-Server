package project.iot.entity;

import project.iot.client.Client;
import project.iot.client.Request;
import project.iot.client.RequestCommand;
import project.iot.client.RequestType;
import project.iot.entity.Actuator;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;
import project.iot.CSVEventReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActuatorTests {

//    @Test
//    public void testActuatorServer() {
//        int port = 12345; // Replace with your desired port number
//        Actuator actuator = new Actuator(21, "an actuator", false);
//        actuator.setOwnEndpoint("localhost", port);
//
//        System.out.println(actuator.getState());
//        System.out.println(actuator.getIP());
//        actuator.acceptServerMessages();
//        System.out.println(actuator.getState());
//    }
}
