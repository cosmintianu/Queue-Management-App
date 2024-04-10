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

    public Server() {

        tasks = new LinkedBlockingQueue<>();
        waitingPeriod = new AtomicInteger(0);
        running = new AtomicBoolean(true);
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
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Task task = tasks.peek();

                if (task != null) {
                    //waitingPeriod.addAndGet(task.getServiceTime());

                    System.out.println(" and remaining time: " + waitingPeriod.get());
                    TimeUnit.SECONDS.sleep(1);
                    waitingPeriod.decrementAndGet();
                    //task.setServiceTime(waitingPeriod.get());
                    task.setServiceTime(task.getServiceTime() - 1);
                    if (task.getServiceTime() == 0) {
                        tasks.poll();
                        System.out.println("Task " + task.getId() + " completed.");
                    }
                    //System.out.println("Task completed:    " + task);
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

}
