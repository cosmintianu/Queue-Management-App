package controller;

import model.Server;
import model.Task;

import java.util.List;

public class ShortestTimeStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        int minWaitingTime = servers.getFirst().getWaitingPeriod().get();
        int minWaitingTimeIndex = 0;
        for(int i = 0; i < servers.size();i++){
            if(servers.get(i).getWaitingPeriod().get() < minWaitingTime){
                minWaitingTimeIndex = i;
            }
        }
        servers.get(minWaitingTimeIndex).addTask(task);
    }
}
