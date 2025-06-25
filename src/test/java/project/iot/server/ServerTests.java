package project.iot.server;

import project.iot.client.Client;
import project.iot.client.Request;
import project.iot.client.RequestCommand;
import project.iot.client.RequestType;
import project.iot.entity.Actuator;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;
import project.iot.CSVEventReader;

import java.util.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTests {

    String csvFilePath = "data/tests/single_client_1000_events_out-of-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void testServerToActuator() {
        Client client = new Client(3, "EMAIL", "localhost", 66666);

        Event event = new ActuatorEvent(0.000111818, 3, 21, "TempSensor", false);
        Filter filter = new Filter(BooleanOperator.EQUALS, false);
        Actuator actuator = new Actuator(21, "an actuator", false);
        actuator.setOwnEndpoint("localhost", 12345);

        Request request = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, filter + actuator.toString());
        Server server = new Server(client);
        server.processIncomingEvent(event);
        server.processIncomingRequest(request);
    }

    @Test
    public void testUpdateMaxWaitTime() {
        Client client = new Client(3, "EMAIL", "localhost", 66666);

        Event event = new ActuatorEvent(0.000111818, 3, 21, "TempSensor", false);
        Filter filter = new Filter(BooleanOperator.EQUALS, false);
        Actuator actuator = new Actuator(21, "an actuator", false);
        actuator.setOwnEndpoint("localhost", 12345);

        Request request = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "5");
        Server server = new Server(client);
        server.processIncomingEvent(event);
        server.processIncomingRequest(request);

        assertEquals(server.getMaxWaitTime(), 5);
    }

    @Test
    public void testToggle() {
        Client client = new Client(3, "EMAIL", "localhost", 66666);

        Event event = new ActuatorEvent(0.000111818, 3, 21, "TempSensor", false);
        Filter filter = new Filter(BooleanOperator.EQUALS, false);
        Actuator actuator = new Actuator(21, "an actuator", false);
        actuator.setOwnEndpoint("localhost", 12345);

        Request request = new Request(RequestType.CONFIG, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, filter + actuator.toString());
        Server server = new Server(client);
        server.processIncomingEvent(event);
        server.processIncomingRequest(request);

    }

    @Test
    public void testLogIf() {
        Client client = new Client(3, "EMAIL", "localhost", 66666);


        Filter filter = new Filter(BooleanOperator.EQUALS, false);
        Request request = new Request(0, RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, filter.toString());
        Server server = new Server(client);

        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }

        server.processIncomingRequest(request);
        assertEquals(server.readLogs().size(), 2);

    }

    @Test
    public void testAddingOutOfOrder(){
        Client client = new Client(3, "EMAIL", "localhost", 66666);
        Server server = new Server(client);
        for(int i = 0; i<20; i++){
            server.processIncomingEvent(eventList.get(i));
        }
    }

    @Test
    public void testGetLatestEvents(){
        Client client = new Client(3, "EMAIL", "localhost", 66666);
        Server server = new Server(client);
        for(int i = 0; i<20; i++){
            server.processIncomingEvent(eventList.get(i));
        }

        Request request = new Request(0, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "5");
        server.processIncomingRequest(request);
        assertEquals("Last 5 events: [Event, Sensor, 1.4611310958862305, 0, 48, TempSensor, 22.57575684993604, Event, Sensor, 1.6689119338989258, 0, 192, TempSensor, 22.106865151701353, Event, Sensor, 1.7831199169158936, 0, 172, CO2Sensor, 403.56671612083875, Event, Sensor, 1.844959020614624, 0, 169, PressureSensor, 1020.9363833850974, Event, Actuator, 1.9090149402618408, 0, 66, Switch, true]",server.getRequestOutput(request) );
    }


    @Test
    public void testComparator(){
        Request normalRequestA = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        Request normalRequestB = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        RequestWithWaitTime requestA = new RequestWithWaitTime(2, normalRequestA);
        RequestWithWaitTime requestB = new RequestWithWaitTime(3, normalRequestB);

        CompareRequestTime comparing = new CompareRequestTime();
        assertEquals(comparing.compare(requestA,requestB), 1);
    }
    @Test
    public void testComparator2(){
        Request normalRequestA = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        Request normalRequestB = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        RequestWithWaitTime requestA = new RequestWithWaitTime(2, normalRequestA);
        RequestWithWaitTime requestB = new RequestWithWaitTime(2, normalRequestB);

        CompareRequestTime comparing = new CompareRequestTime();
        assertEquals(comparing.compare(requestA,requestB), 0);
    }

    @Test
    public void testComparator3(){
        Request normalRequestA = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        Request normalRequestB = new Request(1, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "");
        RequestWithWaitTime requestA = new RequestWithWaitTime(3, normalRequestA);
        RequestWithWaitTime requestB = new RequestWithWaitTime(2, normalRequestB);

        CompareRequestTime comparing = new CompareRequestTime();
        assertEquals(comparing.compare(requestA,requestB), -1);
    }
}
