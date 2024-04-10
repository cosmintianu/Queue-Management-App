package controller;

import model.SelectionPolicy;
import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    public List<Server> servers;
    public List<Thread> threads;
//    private final int maxNoServers;
//    private final int maxTasksPerServer;
    private Strategy strategy;

    public Scheduler(int nrServers,/* int maxTasksPerServer*/ SelectionPolicy selectionPolicy) {
//        this.maxNoServers = nrServers;
//        this.maxTasksPerServer = maxTasksPerServer;
        changeStrategy(selectionPolicy);

        servers = new ArrayList<>();
        threads = new ArrayList<>();
        for (int i = 0; i < nrServers; i++) {
            Server server = new Server(); // Create a new server object
            Thread serverThread = new Thread(server); // Create a thread for the server
            threads.add(serverThread);
            //serverThread.start(); // Start the server thread
            servers.add(server);
        }
    }

    public void startThreads(){
        for(Thread thread : threads){
            thread .start();
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
}
