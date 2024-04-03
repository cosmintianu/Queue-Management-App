import controller.Scheduler;
import model.SelectionPolicy;
import model.Task;
import view.SimulationFrame;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    public int timeLimit = 10;
    public int maxProcessingTime = 10;
    public int minProcessingTime = 2;
    public int numbersOfServers = 2;
    public int numberOfClients = 2;
    public SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;

    private final Scheduler scheduler;
    private final SimulationFrame simulationFrame;
    private final List<Task> generatedTasks;
    private final List<Task> generatedTasksToBePrinted;
    private final AtomicInteger currentTime;

    public SimulationManager() {
        this.currentTime = new AtomicInteger(0);
        this.simulationFrame = new SimulationFrame();
        scheduler = new Scheduler(numbersOfServers, 1,selectionPolicy); // de modificat max tasks per server
        generatedTasks = new ArrayList<>();
        generatedTasksToBePrinted = new ArrayList<>();

    }

    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();
        Thread simulationManagerThread = new Thread(simulationManager);
        simulationManager.generateInitialTasks();

        simulationManagerThread.start();

        try {
            simulationManagerThread.join(); // Wait for simulation manager thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            simulationManagerThread.interrupt(); // Interrupt simulation manager thread
//            try {
//                simulationManagerThread.join(); // Wait for simulation manager thread to finish
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }));
    }

    @Override
    public void run() {
        try {
            // Generate initial tasks


            // Main simulation loop
            while (!Thread.interrupted() && currentTime.get() < timeLimit) {
                // Process tasks arrival
                System.out.println("Time : " + currentTime);
                processArrivalTasks();

                // Update GUI with current simulation state

                updateGUI();

                // Sleep for a period of time (e.g., 1 second) to simulate time passing

                TimeUnit.SECONDS.sleep(1);


                // Increment the current time
                currentTime.incrementAndGet();

                //System.out.println(scheduler.servers.getFirst().getTasks());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop servers gracefully
            scheduler.stopServers();
        }
    }

    public void generateNRandomTasks(){

    }
    // Method to generate initial tasks
    private synchronized void generateInitialTasks() {
        Task task1 = new Task(1, 2, 2);
        Task task2 = new Task(2, 3, 3);
        generatedTasks.add(task1);
        generatedTasks.add(task2);
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
    private synchronized void updateGUI() {
        StringBuilder info = new StringBuilder();
        info.append("Time: ").append(currentTime).append("\n");
        info.append("Waiting clients:").append(generatedTasksToBePrinted).append("\n");

        for (int i = 0; i < scheduler.servers.size(); i++) {
            BlockingQueue<Task> tasks = scheduler.servers.get(i).getTasks();
            info.append("Queue ").append(i + 1).append(": ");
            for (Task task : tasks) {
                info.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",")
                        .append(task.getServiceTime()).append("); ");
            }
            info.append("\n");
        }

        simulationFrame.updateSimulationInfo(info.toString());
    }
}
