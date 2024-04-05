import controller.Scheduler;
import model.SelectionPolicy;
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
    public SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;

    private Scheduler scheduler;
    private final SimulationFrame simulationFrame;
    private final List<Task> generatedTasks;
    private final List<Task> generatedTasksToBePrinted;
    private final AtomicInteger currentTime;
    StringBuilder toBeLogged;
    private final String logFileName = "simulation_log.txt";

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    public SimulationManager() {
        this.currentTime = new AtomicInteger(0);
        this.simulationFrame = new SimulationFrame(this);
//        scheduler = new Scheduler(numbersOfServers, 1, selectionPolicy); // de modificat max tasks per server
        generatedTasks = new ArrayList<>();
        generatedTasksToBePrinted = new ArrayList<>();
        toBeLogged = new StringBuilder();

    }

    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();
        //Thread simulationManagerThread = new Thread(simulationManager);
        //simulationManager.generateInitialTasks();
        //simulationManager.generateNRandomTasks(simulationManager.numberOfClients, simulationManager.minArrivalTime, simulationManager.maxArrivalTime,
                //simulationManager.minProcessingTime, simulationManager.maxProcessingTime);
        //simulationManagerThread.start();


//        try {
//            simulationManagerThread.join(); // Wait for simulation manager thread to finish
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            simulationManagerThread.interrupt(); // Interrupt simulation manager thread
//            try {
//                simulationManagerThread.join(); // Wait for simulation manager thread to finish
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }));

        //System.out.println("in main"  +    simulationManager.currentTime);
    }

    @Override
    public void run() {
        try {
            // Generate initial tasks

//            scheduler.startThreads();
            // Main simulation loop
            while (!Thread.interrupted() && currentTime.get() < timeLimit) {
                // Process tasks arrival
                System.out.println("Time : " + currentTime);
                processArrivalTasks();

                // Update GUI with current simulation state
                //updateGUI();
                logQueue.put(updateGUI().toString());

                // Sleep for a period of time (e.g., 1 second) to simulate time passing

                TimeUnit.SECONDS.sleep(1);


                // Increment the current time
                currentTime.incrementAndGet();

                //System.out.println(scheduler.servers.getFirst().getTasks());
            }
//            logToFile(toBeLogged.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop servers gracefully
            scheduler.stopServers();
        }
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
            //generatedTasksToBePrinted.add(task);
        }
        Collections.sort(generatedTasks, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                return Integer.compare(task1.getArrivalTime(), task2.getArrivalTime());
            }
        });
        generatedTasksToBePrinted.addAll(generatedTasks);
    }

    // Method to generate initial tasks
    private  void generateInitialTasks() {
//        Task task1 = new Task(1, 2, 2);
//        Task task2 = new Task(2, 3, 3);
//        Task task3 = new Task(3, 4, 4);
//        Task task4 = new Task(4, 4, 5);
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

    // Method to process arrival tasks
    private void processArrivalTasks() {
        for (Task task : generatedTasks) {
            if (currentTime.get() >= task.getArrivalTime() && !task.isHasBeenAdded()) {
                scheduler.dispatchTask(task);
//                scheduler.servers.getFirst().addTask(task);
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

        for (int i = 0; i < scheduler.servers.size(); i++) {
            BlockingQueue<Task> tasks = scheduler.servers.get(i).getTasks();
            info.append("Queue ").append(scheduler.servers.get(i).getWaitingPeriod()).append(" ").append(i + 1).append(": ");
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
        //logToFile(info.toString());
    }

    private  void logToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        this.scheduler = new Scheduler(numbersOfServers, 1, selectionPolicy);

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
