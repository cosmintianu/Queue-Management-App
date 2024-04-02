import controller.Scheduler;
import model.SelectionPolicy;
import model.Server;
import model.Task;
import view.SimulationFrame;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    public int timeLimit = 10;
    public int maxProcessingTime = 10;
    public int minProcessingTime = 2;
    public int numbersOfServers = 1;
    public int numberOfClients = 2;
    public SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;

    private Scheduler scheduler;
    private SimulationFrame simulationFrame;
    private List<Task> generatedTasks;
    private AtomicInteger currentTime;

    public SimulationManager(AtomicInteger currentTime) {
        this.simulationFrame = new SimulationFrame();
        scheduler = new Scheduler(1, 1,currentTime);
        generatedTasks = new ArrayList<>();
        this.currentTime = currentTime;
    }

    public static void main(String[] args) {
        AtomicInteger currentTime = new AtomicInteger(0);

        SimulationManager simulationManager = new SimulationManager(currentTime);
        Thread simulationManagerThread = new Thread(simulationManager);
        simulationManagerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            simulationManagerThread.interrupt(); // Interrupt simulation manager thread
            try {
                simulationManagerThread.join(); // Wait for simulation manager thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    @Override
    public void run() {
        try {
            // Generate initial tasks
            generateInitialTasks();

            // Main simulation loop
            while (!Thread.interrupted() && currentTime.get() < timeLimit) {
                // Process tasks arrival
                System.out.println("Time : " + currentTime.get());
                processArrivalTasks();

                // Update GUI with current simulation state
                updateGUI();

                // Sleep for a period of time (e.g., 1 second) to simulate time passing
                TimeUnit.SECONDS.sleep(1);

                // Increment the current time
                currentTime.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop servers gracefully
            scheduler.stopServers();
        }
    }

    // Method to generate initial tasks
    private void generateInitialTasks() {
        Task task1 = new Task(1, 2, 2);
        Task task2 = new Task(2, 3, 3);
        generatedTasks.add(task1);
        generatedTasks.add(task2);
    }

    // Method to process arrival tasks
    private void processArrivalTasks() {
        for (Task task : generatedTasks) {
            if (currentTime.get() >= task.getArrivalTime() && !task.isHasBeenAdded()) {
                scheduler.servers.getFirst().addTask(task);
                task.setHasBeenAdded(true);
            }
        }
    }

    // Method to update GUI with current simulation state
    private void updateGUI() {
        for (int i = 0; i < scheduler.servers.size(); i++) {
            simulationFrame.updateServerQueue(scheduler.servers.get(i).getTasks(), currentTime);
        }
        simulationFrame.setCurrentTime(currentTime);
    }
}
