package model;

public class Task {
    private final int id;
    private final int arrivalTime;
    private int serviceTime;
    boolean hasBeenAdded;

    public Task(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        hasBeenAdded = false;
    }
    @Override
    public String toString() {
        return "(" + id + "," + arrivalTime + "," + serviceTime + ")";
    }

    public boolean isHasBeenAdded() {
        return hasBeenAdded;
    }

    public void setHasBeenAdded(boolean hasBeenAdded) {
        this.hasBeenAdded = hasBeenAdded;
    }

    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }
}
