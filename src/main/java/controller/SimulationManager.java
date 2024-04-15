package controller;

import model.SelectionPolicy;
import model.Server;
import model.Task;
import view.SimulationFrame;


import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
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

    private List<Task> generatedTasks;
    private final AtomicInteger currentTime;
    private final String logFileName = "simulation_log.txt";
    private int peakTime = 0;
    private int maxNumClientsAtCurrentTime = 0;
    private double averageServiceTime;
    private volatile boolean guiThreadRunning = true;

    public SimulationManager() {
        this.currentTime = new AtomicInteger(0);
        this.simulationFrame = new SimulationFrame(this);
        generatedTasks = new ArrayList<>();
    }

    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();
    }

    @Override
    public void run() {
        try {
            while (currentTime.get() <= timeLimit) {
                System.out.println("Time : " + currentTime);

                // Process tasks arrival
                processArrivalTasks();

                computePeakTime(currentTime);

                // Sleep for 1 second to simulate time passing
                TimeUnit.SECONDS.sleep(1);

                // Increment the current time
                currentTime.incrementAndGet();

                // If no more task to be or being processed, stop simulation
                if (!checkIfAnyTasksRemained()) {
                    break;
                }
            }
            stopGUIThread();

            simulationFrame.appendStats(peakTime, computeAverageWaitingTime(), averageServiceTime);
            simulationFrame.writeContentsToFile(logFileName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop servers
            scheduler.stopServers();
        }
    }

    @Override
    public void startSimulation(int maxSimulationTime, int numServers, int numTasks, int minArrivalTime,
                                int maxArrivalTime, int minServiceTime, int maxServiceTime, SelectionPolicy selectionPolicy) {

        this.timeLimit = maxSimulationTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minArrivalTime = minArrivalTime;
        this.maxServiceTime = maxServiceTime;
        this.minServiceTime = minServiceTime;
        this.numbersOfServers = numServers;
        this.numberOfClients = numTasks;

        this.scheduler = new Scheduler(numbersOfServers, selectionPolicy, currentTime);

        generateNRandomTasks(this.numberOfClients, this.minArrivalTime, this.maxArrivalTime,
                this.minServiceTime, this.maxServiceTime);

        averageServiceTime = computeAverageServiceTime();

        Thread simulationManagerThread = new Thread(this);

        simulationManagerThread.start();
        scheduler.startThreads();
        startGUIThread();

    }

    public boolean checkIfAnyTasksRemained() {
        boolean tasksToBeProcessedRemaining = true;
        boolean tasksToBeFinishedRemaining = false;

        if (generatedTasks.isEmpty()) {
            tasksToBeProcessedRemaining = false;
        }

        for (Server server : scheduler.getServers()) {
            if (!server.getTasks().isEmpty()) {
                tasksToBeFinishedRemaining = true;
                break;
            }
        }

        return tasksToBeProcessedRemaining || tasksToBeFinishedRemaining;
    }

    public synchronized void computePeakTime(AtomicInteger currentTime) {
        int sumOfClients = 0;
        for (Server server : scheduler.getServers()) {
            sumOfClients += server.getTasks().size();
        }
        System.out.println("the nr of clients : " + sumOfClients);
        if (sumOfClients > maxNumClientsAtCurrentTime) {
            maxNumClientsAtCurrentTime = sumOfClients;
            peakTime = currentTime.get();
        }
    }

    public double computeAverageWaitingTime() {
        int sumOfWaitingTimes = 0;
        for (Server server : scheduler.getServers()) {
            sumOfWaitingTimes += server.getTotalWaitingTime();
        }
        return (double) sumOfWaitingTimes / numberOfClients;
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
        List<Task> tasks = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            // Generate random arrival time between minArrivalTime and maxArrivalTime
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;

            // Generate random service time between minServiceTime and maxServiceTime
            int serviceTime = random.nextInt(maxServiceTime - minServiceTime + 1) + minServiceTime;

            tasks.add(new Task(i + 1, arrivalTime, serviceTime));
        }
        // Sort the generated tasks by arrival time
        tasks.sort(Comparator.comparingInt(Task::getArrivalTime));
        // Add the sorted tasks to the generatedTasks list
        generatedTasks.addAll(tasks);
    }

    private void processArrivalTasks() {
        Iterator<Task> iterator = generatedTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (currentTime.get() >= task.getArrivalTime() ){
                scheduler.dispatchTask(task);
                iterator.remove(); // Remove the task from generatedTasks
            }
            if (task.getArrivalTime() > currentTime.get()) {
                break;
            }
        }
    }

    // Method to update GUI with current simulation state
    private void updateGUI() {

        StringBuilder info = new StringBuilder();
        info.append("Time: ").append(currentTime).append("\n");
        info.append("Waiting clients:").append(generatedTasks).append("\n");

        for (int i = 0; i < scheduler.getServers().size(); i++) {
            BlockingQueue<Task> tasks = scheduler.getServers().get(i).getTasks();
            info.append("Queue ").append(i + 1).append(": ");
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
    }

    private void startGUIThread() {
        Thread guiThread = new Thread(() -> {
            while (guiThreadRunning) {
                try {
                    SwingUtilities.invokeLater(this::updateGUI);
                    TimeUnit.SECONDS.sleep(1); // Update GUI every second (adjust as needed)

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        guiThread.start();
    }

    private void stopGUIThread() {
        guiThreadRunning = false;
    }
}
