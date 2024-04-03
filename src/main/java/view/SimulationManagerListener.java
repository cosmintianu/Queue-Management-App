package view;

import model.SelectionPolicy;

public interface SimulationManagerListener {
    void startSimulation(
            int maxSimulationTime, int maxProcessingTime, int minProcessingTime,
            int numServers, int numTasks, int minArrivalTime, int maxArrivalTime,
            int minServiceTime, int maxServiceTime, SelectionPolicy selectionPolicy
    );
}
