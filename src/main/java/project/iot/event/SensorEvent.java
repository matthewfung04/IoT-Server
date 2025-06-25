package project.iot.event;

public class SensorEvent implements Event {
    // TODO: Implement this class
    // you can add private fields and methods to this class

    private double timeStamp;
    private int clientId;
    private int entityId;
    private String entityType;
    private double value;

    public SensorEvent(double TimeStamp,
                        int ClientId,
                        int EntityId, 
                        String EntityType, 
                        double Value) {
        this.timeStamp = TimeStamp;
        this.clientId = ClientId;
        this.entityId = EntityId;
        this.entityType = EntityType;
        this.value = Value;
    }

    public double getTimeStamp() {
        // Implement this method
        return timeStamp;
    }

    public int getClientId() {
        // Implement this method
        return clientId;
    }

    public int getEntityId() {
        // Implement this method
        return entityId;
    }

    public String getEntityType() {
        // Implement this method
        return entityType;
    }

    public double getValueDouble() {
        // Implement this method
        return value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "Event, Sensor" +
               ", " + getTimeStamp() +
               ", " + getClientId() +
               ", " + getEntityId() +
               ", " + getEntityType() +
               ", " + getValueDouble();
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof SensorEvent other) {
            return ((this.timeStamp == (other.timeStamp)) &&(this.clientId == other.clientId)
            && (this.entityId == other.entityId) && (this.entityType.equals(other.entityType))
                    && (this.value == other.value));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
