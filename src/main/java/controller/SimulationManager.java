package controller;

import model.SelectionPolicy;
import model.Server;
import model.Task;
import view.SimulationFrame;
import view.SimulationManagerListener;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable, SimulationManagerListener {
    public int timeLimit;
    public int maxArrivalTime;
    public int minArrivalTime;
    public int maxServiceTime;
    public int minServiceTime;
    public int numbersOfServers;
    public int numberOfClients;

    private Scheduler scheduler;
    private final SimulationFrame simulationFrame;
    private final List<Task> generatedTasks;
    private final List<Task> generatedTasksToBePrinted;
    private final AtomicInteger currentTime;
    private final String logFileName = "simulation_log.txt";
    private int peakTime = 0;
    private double averageWaitingTime = 0D;

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    public SimulationManager() {
        this.currentTime = new AtomicInteger(0);
        this.simulationFrame = new SimulationFrame(this);

        generatedTasks = new ArrayList<>();
        generatedTasksToBePrinted = new ArrayList<>();

    }

    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();

    }
    //TO DO : statistics
    //TO DO : stop when no more tasks in the sim
    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && currentTime.get() <= timeLimit) {
                System.out.println("Time : " + currentTime);

                // Process tasks arrival
                processArrivalTasks();

                computeNumberOfClientsAtCurrentTime(currentTime);
                computeAverageWaitingTimeUntilCurrentTime();

                // Update GUI with current simulation state and also add the current output to the .txt
                logQueue.put(updateGUI().toString());
                if(!checkIfAnyTasksRemained()){
                    break;
                }

                // Sleep for 1 second to simulate time passing
                TimeUnit.SECONDS.sleep(1);

                // Increment the current time
                currentTime.incrementAndGet();



            }
            appendStatisticsToLogFile();
            //peaktime
            System.out.println(peakTime);

            //averageWaitingTime
            System.out.println((double) averageWaitingTime/timeLimit);

            simulationFrame.appendStats(peakTime,(double) averageWaitingTime/timeLimit,computeAverageServiceTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop servers gracefully
            scheduler.stopServers();
        }
    }

    private synchronized void appendStatisticsToLogFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
            writer.newLine();
            writer.write("Peak time : ");
            writer.write(Integer.toString(peakTime));
            writer.newLine();
            writer.write("Average waiting time : ");
            writer.write(Double.toString((double) averageWaitingTime / timeLimit));
            writer.newLine();
            writer.write("Average service time : ");
            writer.write(Double.toString((double) computeAverageServiceTime()));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean checkIfAnyTasksRemained(){
        boolean tasksToBeProcessedRemaining = true;
        boolean tasksToBeFinishedRemaining = false;

        if(generatedTasksToBePrinted.isEmpty()){
            tasksToBeProcessedRemaining = false;
        }

        for(Server server : scheduler.getServers()){
            if(!server.getTasks().isEmpty()){
                tasksToBeFinishedRemaining = true;
            }
        }

        if(!tasksToBeProcessedRemaining && !tasksToBeFinishedRemaining){
            return false;
        }
        else return true;
    }
    public synchronized void computeNumberOfClientsAtCurrentTime(AtomicInteger currentTime){
        int sumOfClients = 0;
        for(Server server : scheduler.getServers()){
            sumOfClients += server.getTasks().size();
        }

        if(sumOfClients > peakTime){
            peakTime = currentTime.get();
        }
    }

    public synchronized void computeAverageWaitingTimeUntilCurrentTime(){
        int sumOfWaitingTimes = 0;
        int nrOfNonEmptyServers = 0;

        for(Server server : scheduler.getServers()){
            //if(!server.getTasks().isEmpty()){
                sumOfWaitingTimes += server.getWaitingPeriod().get();
                nrOfNonEmptyServers ++;
            //}
        }

        if(sumOfWaitingTimes > 0){
            averageWaitingTime += sumOfWaitingTimes / nrOfNonEmptyServers;
        }

    }

    public double computeAverageServiceTime() {
        int totalServiceTime = 0;
        for (Task task : generatedTasks) {
            totalServiceTime += task.getServiceTime();
        }
        return (double) totalServiceTime / generatedTasks.size();
    }


    public void generateNRandomTasks(int N, int minArrivalTime, int maxArrivalTime, int minServiceTime, int maxServiceTime) {
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            // Generate random arrival time between minArrivalTime and maxArrivalTime
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;

            // Generate random service time between minServiceTime and maxServiceTime
            int serviceTime = random.nextInt(maxServiceTime - minServiceTime + 1) + minServiceTime;

            Task task = new Task(i + 1, arrivalTime, serviceTime);
            generatedTasks.add(task);
        }
        //Sort the generated tasks by arrival time
        generatedTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
        generatedTasksToBePrinted.addAll(generatedTasks);
    }


    private  void generateInitialTasks() {
        Task task1 = new Task(1, 2, 2);
        Task task2 = new Task(2, 3, 3);
        Task task3 = new Task(3, 4, 4);
        Task task4 = new Task(4, 4, 5);

        generatedTasks.add(task1);
        generatedTasks.add(task2);
        generatedTasks.add(task3);
        generatedTasks.add(task4);

        generatedTasksToBePrinted.addAll(generatedTasks);
    }

    private void processArrivalTasks() {
        for (Task task : generatedTasks) {
            if (currentTime.get() >= task.getArrivalTime() && !task.isHasBeenAdded()) {
                scheduler.dispatchTask(task);

                task.setHasBeenAdded(true);
                generatedTasksToBePrinted.remove(task);
            }
        }
    }

    // Method to update GUI with current simulation state
    private  StringBuilder updateGUI() {
        StringBuilder info = new StringBuilder();
        info.append("Time: ").append(currentTime).append("\n");
        info.append("Waiting clients:").append(generatedTasksToBePrinted).append("\n");

        for (int i = 0; i < scheduler.getServers().size(); i++) {
            BlockingQueue<Task> tasks = scheduler.getServers().get(i).getTasks();
            info.append("Queue ").append(scheduler.getServers().get(i).getWaitingPeriod()).append(" ").append(i + 1).append(": ");
            if (tasks.isEmpty()) {
                info.append("closed");
            }
            for (Task task : tasks) {
                info.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",")
                        .append(task.getServiceTime()).append("); ");
            }
            info.append("\n");
        }

        simulationFrame.updateSimulationInfo(info.toString());
        return info;
    }

    public static void clearFileContents(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write nothing to the file, effectively clearing its contents
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startSimulation(int maxSimulationTime, int numServers,
                                int numTasks, int minArrivalTime,
                                int maxArrivalTime, int minServiceTime,
                                int maxServiceTime, SelectionPolicy selectionPolicy) {
        this.timeLimit = maxSimulationTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minArrivalTime = minArrivalTime;
        this.maxServiceTime = maxServiceTime;
        this.minServiceTime = minServiceTime;
        this.numbersOfServers = numServers;
        this.numberOfClients = numTasks;

        this.scheduler = new Scheduler(numbersOfServers, selectionPolicy);

        clearFileContents(logFileName);

        generateNRandomTasks(numberOfClients,minArrivalTime,maxArrivalTime,
                this.minServiceTime, this.maxServiceTime);

        Thread simulationManagerThread = new Thread(this);
        simulationManagerThread.start();
        scheduler.startThreads();
        startLoggingThread();



    }
    private void startLoggingThread() {
        Thread loggingThread = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
                while (true) {
                    String message = logQueue.take(); // Blocking call to get the log message
                    writer.write(message);
                    writer.newLine();
                    writer.flush(); // Ensure writing to file immediately
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        loggingThread.setDaemon(true); // Set it as a daemon thread to allow program to exit even if this thread is running
        loggingThread.start();
    }
}
