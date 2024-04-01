package view;

import model.Task;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;

public class SimulationFrame extends JFrame {
    private JLabel[] queueLabels;
    private JLabel timeLabel;

    public SimulationFrame(int numberOfServers) {
        setTitle("Queue Management App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create a panel to hold the time label
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timeLabel = new JLabel("Current Time: 0");
        timePanel.add(timeLabel);

        // Add the time panel to the frame
        add(timePanel, BorderLayout.NORTH);

        // Create a panel to hold the queue labels
        JPanel queuePanel = new JPanel(new GridLayout(numberOfServers, 1));
        queueLabels = new JLabel[numberOfServers];
        for (int i = 0; i < numberOfServers; i++) {
            JLabel queueLabel = new JLabel("Queue " + (i + 1) + ": ");
            queuePanel.add(queueLabel);
            queueLabels[i] = queueLabel;
        }

        // Add the queue panel to the frame
        add(queuePanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Update the queue labels with the tasks in the specified server's queue
    public void updateServerQueue(int serverIndex, BlockingQueue<Task> tasks) {
        StringBuilder queueContent = new StringBuilder();
        queueContent.append("Queue ").append(serverIndex + 1).append(": ");
        for (Task task : tasks) {
            queueContent.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",")
                    .append(task.getServiceTime()).append("); ");
        }
        queueLabels[serverIndex].setText(queueContent.toString());
    }

    // Update the current time label
    public void setCurrentTime(int currentTime) {
        timeLabel.setText("Current Time: " + currentTime);
    }
}
