package model;

import java.sql.SQLOutput;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private final AtomicBoolean running;
    AtomicInteger currentTime;


    public Server(AtomicInteger currentTime) {
        tasks = new PriorityBlockingQueue<>(10, Comparator.comparing(Task::getArrivalTime));
        waitingPeriod = new AtomicInteger(0);
        this.currentTime = currentTime;
        running = new AtomicBoolean(true);

    }

    public void addTask(Task newTask) {
        try {
            tasks.put(newTask);
            System.out.println("da am adaugat task");
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
                    int arrivalTime = task.getArrivalTime();
                    int serviceTime = task.getServiceTime();
                    waitingPeriod.set(serviceTime);

                    while (waitingPeriod.get() > 0) {
                        System.out.println("Time in server " + currentTime + " and remaining time: " + waitingPeriod.get());

                        task.setServiceTime(waitingPeriod.get());
                        TimeUnit.SECONDS.sleep(1);
                        waitingPeriod.decrementAndGet();

                    }

                    tasks.poll();
                    System.out.println("Task completed: " + task);
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

    public String generateLog() {
        StringBuilder log = new StringBuilder();
        log.append("Time ").append(currentTime).append("\n");
        log.append("Waiting clients: ");
        for (Task task : tasks) {
            log.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",").append(task.getServiceTime()).append("); ");
        }
        log.append("\n");
        log.append("Queue 1: ");
        if (tasks.isEmpty()) {
            log.append("closed");
        } else {
            for (Task task : tasks) {
                log.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",").append(task.getServiceTime()).append("); ");
            }
        }
        log.append("\n");
        return log.toString();
    }


    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public void setTasks(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }


}
