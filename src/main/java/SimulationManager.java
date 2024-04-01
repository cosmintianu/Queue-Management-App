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
        this.simulationFrame = new SimulationFrame(2);
        scheduler = new Scheduler(1, 1,currentTime);
        generatedTasks = new ArrayList<>();
        this.currentTime = currentTime;
    }

    public static void main(String[] args) {
        AtomicInteger currentTime = new AtomicInteger(0);

        //SimulationFrame simulationFrame = new SimulationFrame(1);

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
        //int currentTime = 0;

        Task task1 = new Task(1, 2, 2);
        Task task2 = new Task(2, 3, 3);
        generatedTasks.add(task1);
        generatedTasks.add(task2);
        while (!Thread.interrupted() && currentTime.get() < timeLimit) {
            try {
                System.out.println("Time: " + currentTime);
                simulationFrame.setCurrentTime(currentTime.get());
                for(Task task : generatedTasks){
                    if(currentTime.get() >= task.getArrivalTime() && !task.isHasBeenAdded()){

                        scheduler.servers.getFirst().addTask(task);
//                        simulationFrame.updateServerQueue(0,scheduler.servers.getFirst().getTasks());
                        //generatedTasks.remove(task);
                        task.setHasBeenAdded(true);
                    }
//                    simulationFrame.updateServerQueue(0,scheduler.servers.getFirst().getTasks());
                }
                simulationFrame.updateServerQueue(0,scheduler.servers.getFirst().getTasks());
                    // Sleep for 1 second to count simulation time


                // Update simulation state, log events, etc.
                currentTime.incrementAndGet();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        scheduler.stopServers();

    }

}
