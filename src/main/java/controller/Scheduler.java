package controller;

import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    private  List<Server> servers;
    private List<Thread> threads;

    private Strategy strategy;

    public Scheduler(int nrServers,SelectionPolicy selectionPolicy, AtomicInteger currentTime) {

        changeStrategy(selectionPolicy);

        servers = new ArrayList<>();
        threads = new ArrayList<>();
        for (int i = 0; i < nrServers; i++) {
            Server server = new Server(currentTime);
            Thread serverThread = new Thread(server);
            threads.add(serverThread);
            servers.add(server);
        }
    }

    public void startThreads() {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    public void changeStrategy(SelectionPolicy selectionPolicy) {
        if (selectionPolicy == SelectionPolicy.SHORTEST_TIME) {
            strategy = new ShortestTimeStrategy();
        } else if (selectionPolicy == SelectionPolicy.SHORTEST_QUEUE) {
            strategy = new ShortestQueueStrategy();

        }
    }

    public void dispatchTask(Task task) {
        strategy.addTask(servers, task);
    }

    public synchronized void stopServers() {
        for (Server server : servers) {
            server.stopServer(); // Stop the server gracefully
        }
    }

    public List<Server> getServers() {
        return servers;
    }

}
