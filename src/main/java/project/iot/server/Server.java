package project.iot.server;

import project.iot.client.RequestCommand;
import project.iot.client.RequestType;
import project.iot.entity.Actuator;
import project.iot.client.Client;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.client.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


public class Server {
    private Client client;
    private double maxWaitTime = 2; // in seconds

    private final Object waitTimeLock = new Object();

    private List<Integer> log = new ArrayList<>();

    private Filter filter;

    private Map<Integer, Integer> entityToEventMap = new HashMap<>(); //Maps eventId to number of events it has

    private List<Event> eventList = new ArrayList<>();

    private PriorityQueue<RequestWithWaitTime> requestQueue = new PriorityQueue<>(new CompareRequestTime());


    private double requestTime;
    // you may need to add additional private fields


    public Server(Client client) {
        // implement the Server constructor
        this.client = client;
        filter = null;
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        // implement this method

        // Important note: updating maxWaitTime may not be as simple as
        // just updating the field. You may need to do some additional
        // work to ensure that events currently being processed are not
        // dropped or ignored by the change in maxWaitTime.
        synchronized (waitTimeLock) {
            this.maxWaitTime = maxWaitTime;
        }
    }


    public double getMaxWaitTime(){
        synchronized (waitTimeLock) {
            return maxWaitTime;
        }
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     * <p>
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter   the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        if (filter.satisfies(eventList.get(eventList.size() - 1))) {
            actuator.updateState(true);

            try {
                Socket socket = new Socket(actuator.getIP(), actuator.getPort());
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                out.print("CONTROL, CONTROL_SET_ACTUATOR_STATE, empty\n");
                out.flush();
                out.close();
                socket.close();

            } catch (IOException e) {

            }
        }

    }

    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     * <p>
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter   the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        if (actuator.getClientId() == client.getClientId()) {
            // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
            synchronized (eventList) {
                int eventListSize = eventList.size() - 1;
                boolean currentState = actuator.getState();

                if (filter.satisfies(eventList.get(eventListSize))) {
                    for (int i = eventListSize; i >= 0; i--) {
                        if (eventList.get(i).getEntityId() == actuator.getId()) {
                            currentState = eventList.get(i).getValueBoolean();
                            break;
                        }
                    }

                    if (currentState) {
                        actuator.updateState(false);
                    } else {
                        actuator.updateState(true);
                    }
                }
            }
        }
    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     * This method only logs the events after the current requests timestamp
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        synchronized (log) {
            log.clear();
            synchronized (eventList) {
                for (Event event : eventList) {
                    if (event.getTimeStamp() >= requestTime && filter.satisfies(event)) {
                        log.add(event.getEntityId());
                    }
                }
            }
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of event IDs
     */
    public List<Integer> readLogs() {
        List<Integer> allLogs;
        synchronized (log) {
            allLogs = new ArrayList<>(log);
            log.clear();
        }
        return allLogs;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        // implement this method
        List<Event> eventsInWindow;
        synchronized (eventList) {
            eventsInWindow = new ArrayList<>();
            for (Event event : eventList) {
                if ((event.getTimeStamp() >= timeWindow.getStartTime()) && (event.getTimeStamp() <= timeWindow.getEndTime())) {
                    eventsInWindow.add(event);
                }
            }
        }
        return eventsInWindow;

    }

    /**
     * Returns a set of IDs for all the entities of the client for which
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     *
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        // implement this method
        Set<Integer> entityIds = new HashSet<>();
        synchronized (eventList) {
            for (Event event : eventList) {
                entityIds.add(event.getEntityId());
            }
        }
        return new ArrayList<>(entityIds);
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        List<Event> result = new ArrayList<>();

        synchronized (eventList) {
            // Iterate over the first N entries (last N events)
            for (int i = n; i >= 1; i--) {
                Event event = eventList.get(eventList.size() - i);

                result.add(event);
            }
        }
        return result;
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     * <p>
     * If there was a tie, then this method should return the largest ID.
     *
     * @return the most active entity ID of the client, gives -1 if no events have occurred
     */
    public int mostActiveEntity() {
        // implement this method

        int mostActiveId = -1;
        int mostActiveNumEvents = -1;

        //Key is ID and value is number of events
        synchronized (entityToEventMap) {
            for (Map.Entry<Integer, Integer> entry : entityToEventMap.entrySet()) {

                if (entry.getValue() > mostActiveNumEvents) {
                    mostActiveNumEvents = entry.getValue();
                    mostActiveId = entry.getKey();
                } else if ((entry.getValue() == mostActiveNumEvents) && entry.getKey() > mostActiveId) {
                    mostActiveNumEvents = entry.getValue();
                    mostActiveId = entry.getKey();
                }
            }
        }
        return mostActiveId;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * <p>
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n        the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean()
     * based on the type of the entity. That is why the return type is List<Object>.
     * <p>
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n        the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * Adds an event to the event list in order determined by their timestamp using binary search
     *
     * @param event, the event to be added to eventList
     */
    public void addEvent(Event event) {
        double eventTimeStamp = event.getTimeStamp();
        int insertionIndex = 0;
        synchronized (eventList) {
            if (!eventList.isEmpty()) {
                int start = 0;
                int end = eventList.size();
                while (start <= end) {
                    insertionIndex = (start + end) / 2;
                    double timeStampAtIndex = eventList.get(insertionIndex).getTimeStamp();
                    if (timeStampAtIndex == eventTimeStamp) {
                        break;
                    } else if (eventTimeStamp < timeStampAtIndex) {
                        if (insertionIndex == 0) {
                            break;
                        } else if (eventList.get(insertionIndex - 1).getTimeStamp() <= eventTimeStamp) {
                            break;
                        } else {
                            end = insertionIndex;
                        }
                    } else {
                        if (insertionIndex == eventList.size() - 1) {
                            insertionIndex++;
                            break;
                        } else if (eventList.get(insertionIndex + 1).getTimeStamp() >= eventTimeStamp) {
                            insertionIndex++;
                            break;
                        } else {
                            start = insertionIndex;
                        }
                    }
                }
            }
            eventList.add(insertionIndex, event);
        }
    }

    public void processIncomingEvent(Event event) {
        addEvent(event);
        synchronized (entityToEventMap) {
            if (entityToEventMap.get(event.getEntityId()) == null) {
                entityToEventMap.put(event.getEntityId(), 1);
            } else {
                entityToEventMap.put(event.getEntityId(), entityToEventMap.get(event.getEntityId()) + 1);
            }
        }

    }

    /**
     *
     * This method adds the incoming request into this server's eventList.
     *
     *
     *  @param request is not null
     */
    public void processIncomingRequest(Request request) {
        synchronized (requestQueue) {
            synchronized (waitTimeLock) {
                requestQueue.add(new RequestWithWaitTime(maxWaitTime, request));
            }
        }
        RequestWithWaitTime currentRequest;
        synchronized (requestQueue) {
            currentRequest = requestQueue.poll();
        }
        assert currentRequest != null;
        getRequestOutput(currentRequest.getRequest());
    }


    /**
     *
     * Reads a serialized string representing a filter and converts it to a filter
     *
     * @param filterData
     * @return a filter that has been created from the string of serialized information
     */
    public Filter readRequestDataFilter(String filterData) {

        filterData = filterData.substring(0, filterData.length() - 1);

        String[] filters = filterData.split("-");
        List<Filter> filterList = new ArrayList<>();

        for (String filter : filters) {
            String[] params = filter.split(",");

            if (params[0].equals("null")) {
                filterList.add(new Filter(params[4], DoubleOperator.valueOf(params[1]), Double.valueOf(params[3])));
            } else {
                filterList.add(new Filter(BooleanOperator.valueOf(params[0]), Boolean.valueOf(params[2])));
            }
        }

        return new Filter(filterList);
    }

    /**
     *
     * Gets the output of a request in the form of a string to the client
     *
     * @param request requires not null
     * @return a string representing the output of a given request
     */
    public String getRequestOutput(Request request) {
        String requestData = request.getRequestData();
        requestTime = request.getTimeStamp();

        switch (request.getRequestCommand()) {
            case CONFIG_UPDATE_MAX_WAIT_TIME:
                double newWaitTime = Double.valueOf(request.getRequestData());
                updateMaxWaitTime(newWaitTime);
                break;

            case CONTROL_SET_ACTUATOR_STATE:
                String actuatorData = requestData.substring(requestData.indexOf("{") + 1, requestData.indexOf("}"));
                String filterData = requestData.substring(requestData.indexOf("[") + 1, requestData.indexOf("]"));
                Filter filter = readRequestDataFilter(filterData);

                String[] params = actuatorData.split(",");
                Actuator actuator = new Actuator(Integer.valueOf(params[0]), Integer.valueOf(params[1]), params[2], Boolean.valueOf(params[5]));
                actuator.setOwnEndpoint(params[3], Integer.valueOf(params[4]));
                setActuatorStateIf(filter, actuator);

                break;
            case CONTROL_TOGGLE_ACTUATOR_STATE:
                String actuatorDataToggle = requestData.substring(requestData.indexOf("{") + 1, requestData.indexOf("}"));
                String filterDataToggle = requestData.substring(requestData.indexOf("[") + 1, requestData.indexOf("]"));
                Filter filterToggle = readRequestDataFilter(filterDataToggle);

                String[] paramsToggle = actuatorDataToggle.split(",");
                Actuator actuatorToggle = new Actuator(Integer.valueOf(paramsToggle[0]), Integer.valueOf(paramsToggle[1]), paramsToggle[2], Boolean.valueOf(paramsToggle[5]));
                actuatorToggle.setOwnEndpoint(paramsToggle[3], Integer.valueOf(paramsToggle[4]));
                toggleActuatorStateIf(filterToggle, actuatorToggle);
                break;

            case CONTROL_NOTIFY_IF:
                String filterLog = requestData.substring(requestData.indexOf("[") + 1, requestData.indexOf("]"));
                Filter filterLogIf = readRequestDataFilter(filterLog);
                logIf(filterLogIf);
                break;
            case ANALYSIS_GET_EVENTS_IN_WINDOW:
                String[] splitRequestData = requestData.split(",");
                return "All events in timeWindow: " + eventsInTimeWindow(new TimeWindow(Double.valueOf(splitRequestData[0]), Double.valueOf(splitRequestData[1]))).toString();

            case ANALYSIS_GET_ALL_ENTITIES:
                return "All entities: " + getAllEntities().toString();

            case ANALYSIS_GET_LATEST_EVENTS:
                int nEvents = Integer.valueOf(request.getRequestData());
                return "Last " + nEvents + " events: " + lastNEvents(nEvents).toString();

            case ANALYSIS_GET_MOST_ACTIVE_ENTITY:
                return "Most Active Entity: " + mostActiveEntity();
        }

        return "";
    }

    /**
     * Uncomment to test network capabilities between actuator and server
     */
    public static void main(String[] args) {
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
}
