package controller;

import model.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    public List<Server> servers;
    private int maxNoServers;
    private int maxTasksPerServer;
    private Strategy strategy;

    AtomicInteger currentTime;

    public Scheduler(int maxNoServers, int maxTasksPerServer, AtomicInteger currentTime) {
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.currentTime = currentTime;
        servers = new ArrayList<>();
        for (int i = 0; i < maxNoServers; i++) {
            Server server = new Server(currentTime); // Create a new server object
            Thread serverThread = new Thread(server); // Create a thread for the server
            serverThread.start(); // Start the server thread
            servers.add(server);
        }
    }
    public void stopServers() {
        for (Server server : servers) {
            server.stopServer(); // Stop the server gracefully
        }
    }
}
