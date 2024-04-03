package controller;

import model.Server;
import model.Task;

import java.util.List;

public class ShortestQueueStrategy implements Strategy{

public void addTask(List<Server> servers, Task task) {
    int minElemSize = servers.getFirst().getTasks().size();
    int minElemSizeIndex = 0;
    for(int i = 0; i < servers.size();i++){
        if(servers.get(i).getTasks().size() < minElemSize){
            minElemSizeIndex = i;
        }
    }
    servers.get(minElemSizeIndex).addTask(task);
}
}