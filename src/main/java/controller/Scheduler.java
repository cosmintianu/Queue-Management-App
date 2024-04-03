package controller;

import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    public List<Server> servers;
    private int maxNoServers;
    private int maxTasksPerServer;
    private Strategy strategy;

    public Scheduler(int nrServers, int maxTasksPerServer,SelectionPolicy selectionPolicy) {
        this.maxNoServers = nrServers;
        this.maxTasksPerServer = maxTasksPerServer;
        changeStrategy(selectionPolicy);

        servers = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < nrServers; i++) {
            Server server = new Server(); // Create a new server object
            Thread serverThread = new Thread(server); // Create a thread for the server
            serverThread.start(); // Start the server thread
            servers.add(server);
        }
    }
    public void changeStrategy(SelectionPolicy selectionPolicy) {
        if (selectionPolicy == SelectionPolicy.SHORTEST_TIME) {
            strategy = new ShortestTimeStrategy();
        } else if (selectionPolicy == SelectionPolicy.SHORTEST_QUEUE) {
            strategy = new ShortestQueueStrategy();

        }
    }
    public void dispatchTask(Task task){
        strategy.addTask(servers,task);
    }

    public synchronized void stopServers() {
        for (Server server : servers) {
            server.stopServer(); // Stop the server gracefully
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
