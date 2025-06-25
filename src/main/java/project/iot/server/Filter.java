package project.iot.server;

import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;

import java.util.ArrayList;
import java.util.List;

import static project.iot.server.DoubleOperator.*;

enum DoubleOperator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

enum BooleanOperator {
    EQUALS,
    NOT_EQUALS
}

public class Filter {
    // you can add private fields and methods to this class

    private BooleanOperator boolOperator = null;
    private DoubleOperator doubleOperator = null;
    private boolean boolValue;
    private double doubleValue;
    private String field;
    private List<Filter> filterList = new ArrayList<>();
    private boolean isBooleanFilter = false;
    private boolean isDoubleFilter = false;
    private boolean isTimeStampFilter = false;
    private boolean isComplexFilter = false;


    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * 
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        // TODO: implement this method
        this.boolOperator = operator;
        this.boolValue = value;
        filterList.add(this);
        this.isBooleanFilter = true;
        field = "NONE";
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * 
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * 
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value the double value to match
     *
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        // TODO: implement this method
        this.field = field;
        this.doubleOperator = operator;
        this.doubleValue = value;
        filterList.add(this);
        if(field.equals("value")){isDoubleFilter = true;};
        if(field.equals("timestamp")){isTimeStampFilter = true;};
    }
    
    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        // TODO: implement this method
        isComplexFilter = true;

        filterList.addAll(getFilterHelper(filters));
    }

    /**
     *
     * A recursive helper for complex filters (filters in filters in filters...)
     *
     * @param filters is not null and is not empty
     * @return a list of filters that is equivlent to the list containing the complex filters
     */
    private List<Filter> getFilterHelper(List<Filter> filters){
        List<Filter> outputList = new ArrayList<>();

        for(Filter filter : filters){
            if(filter.getIsComplexFilter()){
                outputList.addAll(filter.getFilterHelper(filter.getFilterList()));
            } else{
                outputList.add(filter);
            }
        }
        return outputList;
    }

    public boolean getIsDoubleFilter(){
        return isDoubleFilter;
    }

    public boolean getIsBooleanFilter() {
        return isBooleanFilter;
    }

    public DoubleOperator getDoubleOperator(){
        return doubleOperator;
    }

    public BooleanOperator getBoolOperator(){
        return boolOperator;
    }

    public boolean getIsTimeStampFilter(){
        return isTimeStampFilter;
    }

    public boolean getBooleanValue(){
        return boolValue;
    }

    public double getDoubleValue(){
        return doubleValue;
    }

    public boolean getIsComplexFilter(){
        return isComplexFilter;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        boolean filterSatisfies = false;

        double val;
        for(Filter filter : filterList) {

            if (filter.getIsDoubleFilter() || filter.getIsTimeStampFilter()) {
                if (filter.getIsDoubleFilter()) {
                    val = event.getValueDouble();
                    if (event instanceof ActuatorEvent) {
                        return false;
                    } //Actuator doesn't have double values
                } else {
                    val = event.getTimeStamp();
                }
                switch (filter.getDoubleOperator()) {
                    case EQUALS:
                        if (val == filter.getDoubleValue()) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                    case GREATER_THAN:
                        if (val > filter.getDoubleValue()) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                    case LESS_THAN:
                        if (val < filter.getDoubleValue()) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                    case GREATER_THAN_OR_EQUALS:
                        if (val >= filter.getDoubleValue()) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                    case LESS_THAN_OR_EQUALS:
                        if (val <= filter.getDoubleValue()) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                }
                if (!filterSatisfies) {
                    return false;
                }
                ;
            } else { //if it's a boolean filter
                if (event instanceof SensorEvent) {
                    return false; //sensor event doesn't have boolean value
                }
                switch (filter.getBoolOperator()) {
                    case EQUALS:
                        if (event.getValueBoolean() == filter.boolValue) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                    case NOT_EQUALS:
                        if (event.getValueBoolean() != filter.boolValue) {
                            filterSatisfies = true;
                        } else {
                            filterSatisfies = false;
                        }
                        ;
                        break;
                }
                if (!filterSatisfies) {
                    return false;
                }
                ;
            }

        }

        return filterSatisfies;
    }

    public boolean filterListEmpty() {
        return filterList.isEmpty();
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        // TODO: implement this method
        for(Event event : events){
            if(!satisfies(event)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        // TODO: implement this method
        if(!satisfies(event)) {
            return null;
        } else {
            if(event instanceof ActuatorEvent) {
                return new ActuatorEvent(event.getTimeStamp(), event.getClientId(), event.getEntityId(), event.getEntityType(), event.getValueBoolean());
            } else {
                return new SensorEvent(event.getTimeStamp(), event.getClientId(), event.getEntityId(), event.getEntityType(), event.getValueDouble());
            }
        }
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     *        or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        // TODO: implement this method
        List<Event> eventList = new ArrayList<>();
        for(Event event : events){
            Event eventToAdd = sift(event);
            if(eventToAdd != null){
                eventList.add(eventToAdd);
            }
        }
        return eventList;
    }

    public List<Filter> getFilterList(){
        return filterList;
    }
    @Override
    public String toString() {
        return "[" + toStringHelper() + "]"; //we know all filter data is between [...]
    }

    /**
     * Translates all the filters into information
     *
     * @return a string containing filter information
     */
    private String toStringHelper(){
        StringBuilder str = new StringBuilder();
        for(Filter filter : filterList) {
            if(filter.getFilterList().size() > 1){
                str.append(filter.toStringHelper());
            }
            str.append( "" + filter.boolOperator +
                    "," + filter.doubleOperator +
                    "," + filter.boolValue +
                    "," + filter.doubleValue +
                    "," + filter.field + "-");
        }

        return str.toString();
    }


}
