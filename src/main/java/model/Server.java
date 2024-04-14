package model;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private final BlockingQueue<Task> tasks;
    private final AtomicInteger waitingPeriod;
    private final AtomicBoolean running;
    private int totalWaitingTime;
    private final AtomicInteger currentTime;

    public Server(AtomicInteger currentTime) {

        tasks = new LinkedBlockingQueue<>();
        waitingPeriod = new AtomicInteger(0);
        running = new AtomicBoolean(true);
        this.currentTime = currentTime;
        totalWaitingTime = 0;
    }

    public void addTask(Task newTask) {
        try {
            tasks.put(newTask);
            waitingPeriod.addAndGet(newTask.getServiceTime());

            System.out.println("Task has been added");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        int prevTaskId = 0;
        while (running.get() && !Thread.currentThread().isInterrupted()) {


            try {
                Task task = tasks.peek();

                if (task != null) {
                    int currentTaskId = task.getId();

                    if(prevTaskId != currentTaskId){
                        totalWaitingTime += currentTime.get() - task.getArrivalTime();
                        //System.out.println("The task waited needs to wait for : " + totalWaitingTime);
                    }
                    System.out.println("the curr time in the task is " + currentTime);
                    prevTaskId = currentTaskId;

                    System.out.println("Task " + task.getId() + " has remaining time: " + task.getServiceTime());
                    TimeUnit.SECONDS.sleep(1);
                    waitingPeriod.decrementAndGet();

                    task.setServiceTime(task.getServiceTime() - 1);

                    if (task.getServiceTime() == 0) {
                        tasks.poll();
                        System.out.println("Task " + task.getId() + " completed.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error processing task: " + e.getMessage());
            }
        }
    }

    public void stopServer() {
        running.set(false);
        Thread.currentThread().interrupt(); // Interrupt the server thread
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public int getTotalWaitingTime() {
        return totalWaitingTime;
    }
}
