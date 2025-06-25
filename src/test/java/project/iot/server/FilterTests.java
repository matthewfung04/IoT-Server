package project.iot.server;

import project.iot.CSVEventReader;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;
import static project.iot.server.BooleanOperator.EQUALS;
import static project.iot.server.DoubleOperator.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FilterTests{

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    @Test
    public void testFilterTimeStampSingleEvent() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", false);
        Filter timeStampFilter = new Filter("timestamp", GREATER_THAN, 0.0);
        assertTrue(timeStampFilter.satisfies(event1));
        assertTrue(timeStampFilter.satisfies(event2));
    }

    @Test
    public void testFilterBooleanValueSingleEvent() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", true);
        Filter booleanFilter = new Filter(BooleanOperator.EQUALS, true);
        assertFalse(booleanFilter.satisfies(event1));
        assertTrue(booleanFilter.satisfies(event2));
    }

    @Test
    public void testBooleanFilter() {
        Event actuatorEvent = eventList.get(3);
        Filter sensorFilter = new Filter(EQUALS, false);
        assertEquals(true, sensorFilter.satisfies(actuatorEvent));
    }

    @Test
    public void testDoubleFilterTS() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("timestamp", LESS_THAN, 1);
        assertEquals(true, sensorFilter.satisfies(sensorEvent));
    }


    @Test
    public void testDoubleFilterValue() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        assertEquals(false, sensorFilter.satisfies(sensorEvent));
    }

    @Test
    public void testComplexFilter() {
        Event sensorEvent = eventList.get(1);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(true, complexFilter.satisfies(sensorEvent));
    }

    @Test
    public void testMultiEventSatisfies() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(false, complexFilter.satisfies(eventsList));
    }

    @Test
    public void testTrueMultiEventSatisfies() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        assertEquals(true, sensorTSFilter.satisfies(eventsList));
    }

    @Test
    public void testSift() {
        Event sensorEvent = eventList.get(1);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(sensorEvent, complexFilter.sift(sensorEvent));
    }

    @Test
    public void testMultiEventSift() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        List<Event> filteredEvents = new ArrayList<>();
        filteredEvents.add(eventList.get(1));
        filteredEvents.add(eventList.get(2));
        assertEquals(filteredEvents, complexFilter.sift(eventsList));
    }

    @Test
    public void testVeryComplexFilter() {
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        Filter sensorValueFilter1 = new Filter("value", GREATER_THAN, 69);
        Filter sensorTSFilter2 = new Filter("timestamp", GREATER_THAN, 420);
        List<Filter> filterList2 = new ArrayList<>();
        filterList2.add(sensorValueFilter1);
        filterList2.add(sensorTSFilter2);
        Filter complexFilter2 = new Filter(filterList2);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(complexFilter2);
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        System.out.println(complexFilter);
        assertEquals(complexFilter.getFilterList().size(), 4);
    }

    @Test
    public void testSatisfiesEquals(){
        Filter filter = new Filter("value", DoubleOperator.EQUALS, 45);
        Event event = new SensorEvent(1,1,1,"f",45);
        assertTrue(filter.satisfies(event));
    }

    @Test
    public void testNotSatisfiesEquals(){
        Filter filter = new Filter("value", DoubleOperator.EQUALS, 45.01);
        Event event = new SensorEvent(1,1,1,"f",45);
        assertFalse(filter.satisfies(event));
    }

    @Test
    public void testLessThanOrEquals(){
        Filter filter = new Filter("value", LESS_THAN_OR_EQUALS, 45);
        Event event = new SensorEvent(1,1,1,"f",20);
        assertTrue(filter.satisfies(event));
    }

    @Test
    public void testNotLessThanOrEquals(){
        Filter filter = new Filter("value", LESS_THAN_OR_EQUALS, 45);
        Event event = new SensorEvent(1,1,1,"f",79);
        assertFalse(filter.filterListEmpty());
        assertFalse(filter.satisfies(event));
    }

    @Test
    public void testNotEqualsBooleanFilter(){
        Filter filter = new Filter( BooleanOperator.NOT_EQUALS, true);
        Event event = new ActuatorEvent(1,1,1,"f",false);
        assertTrue(filter.satisfies(event));
    }

    @Test
    public void testNotEqualsBooleanFilter2(){
        Filter filter = new Filter( BooleanOperator.NOT_EQUALS, true);
        Event event = new ActuatorEvent(1,1,1,"f",true);
        assertFalse(filter.satisfies(event));
    }

    @Test
    public void testNotGreaterThan(){
        Filter filter = new Filter("value", GREATER_THAN_OR_EQUALS, 45);
        Event event = new SensorEvent(1,1,1,"f",20);
        assertFalse(filter.satisfies(event));
    }

    @Test
    public void testNotLessThan(){
        Filter filter = new Filter("value", LESS_THAN, 45);
        Event event = new SensorEvent(1,1,1,"f",100);
        assertFalse(filter.satisfies(event));
    }

    
}

