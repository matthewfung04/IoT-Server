package project.iot;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVEventReader {
    String filePath;

    public CSVEventReader(String filePath) {
        this.filePath = filePath;
    }

    public void  updateFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<Event> readEvents() {
        List<Event> events = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            boolean skipHeader = true;
            for (String[] row : rows) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                double timeStamp = Double.parseDouble(row[0]);
                int clientId = Integer.parseInt(row[1]);
                int entityId = Integer.parseInt(row[2]);
                String entityType = row[3];
                if (row[4].equals("boolean")){

                    int intValue =  Integer.parseInt(row[5]);
                    boolean valueBoolean = (intValue == 0) ? false : true;
                    Event event = new ActuatorEvent(timeStamp, clientId, entityId, entityType, valueBoolean);
                    events.add(event);
                }
                else {
                    double valueDouble = Double.parseDouble(row[5]);
                    Event event = new SensorEvent(timeStamp, clientId, entityId, entityType, valueDouble);
                    events.add(event);
                }

            }

        } catch (IOException | CsvException e) {
            e.printStackTrace(); // Handle the exception appropriately based on your application
        }

        return events;
    }
}
