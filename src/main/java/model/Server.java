package model;

import view.SimulationFrame;

import java.sql.SQLOutput;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private final AtomicBoolean running;

    public Server() {

        tasks = new LinkedBlockingQueue<>();
        waitingPeriod = new AtomicInteger(0);
        running = new AtomicBoolean(true);
    }

    public synchronized void addTask(Task newTask) {
        try {
            tasks.put(newTask);
            waitingPeriod.addAndGet(newTask.getServiceTime());
//            if (waitingPeriod.get() == 0) {
//                waitingPeriod.set(newTask.getServiceTime());
//            }
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
                    //waitingPeriod.addAndGet(task.getServiceTime());

                    System.out.println(" and remaining time: " + waitingPeriod.get());
                    Thread.sleep(1000);
                    waitingPeriod.decrementAndGet();
                    //task.setServiceTime(waitingPeriod.get());
                    task.setServiceTime(task.getServiceTime() - 1);
                    if (task.getServiceTime() == 0) {
                        tasks.poll();

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
