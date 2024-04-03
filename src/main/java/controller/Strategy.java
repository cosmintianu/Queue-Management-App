package controller;

import model.Server;
import model.Task;

import java.util.List;

public interface Strategy {
     void addTask(List<Server> server, Task task);
}
