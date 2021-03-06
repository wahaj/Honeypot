package def;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class ActivityEngine {

    String eventFileName;
    String statFileName;
    String outputFileName;
    int noOfDays;

    // ArrayList containg all the events and their info
    ArrayList<EventInfo> eventInfos;
    // ArrayList containing all the generated log events with a one-to-one index mapping to the EventInfo ArrayList
    ArrayList<Event> events;



    public ActivityEngine(String eventFile, String statFile, String outputFile, int days) throws FileNotFoundException {
        eventFileName = eventFile;
        statFileName = statFile;
        outputFileName = outputFile;
        noOfDays = days;

        readEventsFromFile();
        readStatsFromFile();

        events = new ArrayList<Event>(eventInfos.size());
        for (EventInfo eventInfo : eventInfos) {
            events.add(new Event(eventInfo.name));
        }

        generateNormalEventOccurrences();
    }

    public void readEventsFromFile() throws FileNotFoundException {

        File eventFile = new File(eventFileName);
        Scanner eventScanner = new Scanner(eventFile);

        int lineNo = 0;

        System.out.println("\n==============================================\n");

        System.out.println("Loading Events");
        while (eventScanner.hasNextLine()) {
            String currentLine = eventScanner.nextLine();
            if (lineNo == 0) {
                int num = Integer.parseInt(currentLine);
                eventInfos = new ArrayList<EventInfo>(num);
            } else {
                System.out.println("----------------------------");
                String[] tokens = currentLine.split(":");
                eventInfos.add(getEventInfoFromToken(tokens));

                System.out.println(String.format("> Added Event %s", eventInfos.get(eventInfos.size()-1).name));
            }
            lineNo++;
        }
        System.out.println("\n==============================================");

    }

    public void readStatsFromFile() throws FileNotFoundException {
        File statFile = new File(statFileName);
        Scanner statScanner = new Scanner(statFile);

        int lineNo = 0;

        System.out.println("\n==============================================\n");

        System.out.println("Loading Statistic");
        while (statScanner.hasNextLine()) {
            String currentLine = statScanner.nextLine();
            if (currentLine.equals(" "))
                break;
            if (lineNo == 0) {
                int num = Integer.parseInt(currentLine);
            } else {
                System.out.println("----------------------------");
                String[] tokens = currentLine.split(":");
                setEventInfoStatisticFromToken(tokens);
                System.out.println(String.format("> Statistic added to Event %s", eventInfos.get(eventInfos.size()-1).name));
            }
            lineNo++;
        }
        System.out.println("\n==============================================\n");

    }

    private EventInfo getEventInfoFromToken(String[] tokens) {
        String name = "";
        EventDataType datatype = null;
        double min = 0.0;
        double max = 0.0;
        int weight = 0;

        for (int tokenNo = 0; tokenNo < tokens.length; tokenNo++) {
            switch (tokenNo) {
                case 0:
                    name = tokens[tokenNo];
                    break;
                case 1:
                    if (tokens[tokenNo].equals("C")) {
                        datatype = EventDataType.CONTINOUS;
                    } else if (tokens[tokenNo].equals("D")) {
                        datatype = EventDataType.DISCRETE;
                    } else {
                        System.out.println("Event Type is Unknown");
                    }
                    break;
                case 2:
                    if (tokens[tokenNo].equals("")) {
                        min = 0;
                    }
                    else if (datatype == EventDataType.CONTINOUS) {
                        min = Double.parseDouble(tokens[tokenNo]);
                    } else if (datatype == EventDataType.DISCRETE) {
                        min = Integer.parseInt(tokens[tokenNo]);
                    }
                    break;
                case 3:
                    if (tokens[tokenNo].equals("")) {
                        max = 0;
                    }
                    else if (datatype == EventDataType.CONTINOUS) {
                        max = Double.parseDouble(tokens[tokenNo]);
                    }
                    else if (datatype == EventDataType.DISCRETE) {
                        max = Integer.parseInt(tokens[tokenNo]);
                    }
                case 4:
                    if (tokens[tokenNo].equals("")) {

                    } else {
                        weight = Integer.parseInt(tokens[tokenNo]);
                        break;
                    }
            }
        }
        return new EventInfo(name,datatype, min, max, weight);
    }


    public void generateNormalEventOccurrences() {
        EventInfo currentEventInfo;
        Random random = new Random();

        System.out.println("Running Activity Simulation Engine");
        System.out.println("---------------------------------------------");

        // Generating a normally distributed log event

        for (int currentEvent = 0; currentEvent < events.size(); currentEvent++) {
            currentEventInfo = eventInfos.get(currentEvent);
            for (int currentDay=0; currentDay<noOfDays; currentDay++) {
                System.out.println(String.format("Adding data for %s - Day %d", currentEventInfo.name, currentDay+1));
                double value = random.nextGaussian()*currentEventInfo.stdDev + currentEventInfo.mean;
                events.get(currentEvent).addOccurrence(value);
                if (currentEventInfo.type == EventDataType.CONTINOUS)
                    System.out.println(String.format("Generated Data: %f", value));
                else
                    System.out.println(String.format("Generated Data: %d", ((int) value)));

            }
            System.out.println("========================================\n");
        }
    }

    public void setEventInfoStatisticFromToken(String[] tokens) {
        String name;
        double mean = 0.0;
        double stdDev = 0.0;

        EventInfo eventInfo = null;


        for (int tokenNo = 0; tokenNo < tokens.length; tokenNo++) {
            switch (tokenNo) {
                case 0:
                    name = tokens[tokenNo];
                    for (int i = 0; i < eventInfos.size(); i++) {
                        if (eventInfos.get(i).name.equals(name)) {
                            eventInfo = eventInfos.get(i);
                        }
                    }
                    break;
                case 1:
                    mean = Double.parseDouble(tokens[tokenNo]);
                    break;
                case 2:
                    stdDev = Double.parseDouble(tokens[tokenNo]);
                    break;
                default:
                    System.out.println("Error, out of range data");
            }
        }
        eventInfo.setStatistic(mean, stdDev);
    }

    
    public ArrayList<Event> getGeneratedEvents() {
        return events;
    }
    public ArrayList<EventInfo> getEventsInfos() {
        return eventInfos;
    }


    public void writeGeneratedEventsToFile(String outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int currentEvent = 0; currentEvent < events.size(); currentEvent++) {
            printWriter.printf("---\n");
            Event event = events.get(currentEvent);
            printWriter.printf("%s\n", event.name);
            for (int currentDay=0; currentDay < event.occurrences.size(); currentDay++) {
                if (eventInfos.get(currentEvent).type == EventDataType.CONTINOUS)
                    printWriter.printf("Day %d: %f\n", currentDay, event.occurrences.get(currentDay));
                else if (eventInfos.get(currentEvent).type == EventDataType.DISCRETE)
                    printWriter.printf("Day %d: %d\n", currentDay, Math.round(event.occurrences.get(currentDay)));

            }
            printWriter.printf("Mean: %f\n" , event.getMean());
            printWriter.printf("SD: %f\n" , event.getStdDev());
        }
        printWriter.close();
    }
}