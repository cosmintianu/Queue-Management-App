package view;

import model.Task;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationFrame extends JFrame {
    private JTextArea textArea;
    private JLabel timeLabel;

    public SimulationFrame() {
        setTitle("Queue Management App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create a panel to hold the time label
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timeLabel = new JLabel("Current Time: 0");
        timePanel.add(timeLabel);

        // Add the time panel to the frame
        add(timePanel, BorderLayout.NORTH);

        // Create a text area to display the queue status
        textArea = new JTextArea();
        textArea.setEditable(false); // Make it non-editable
        JScrollPane scrollPane = new JScrollPane(textArea); // Add scroll pane
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // Update the text area with the tasks in the queues for all servers
    public synchronized void updateServerQueue(BlockingQueue<Task> tasks, AtomicInteger currentTime) {
        StringBuilder content = new StringBuilder();
        content.append(textArea.getText()); // Append existing content
        content.append("\nTime: ").append(currentTime.get()).append("\n");
        content.append("Queue: ");

        for (Task task : tasks) {
            content.append("(").append(task.getId()).append(",").append(task.getArrivalTime()).append(",")
                    .append(task.getServiceTime()).append("); ");
        }
        content.append("\n");

        setCurrentTime(currentTime);
        textArea.setText(content.toString());
    }


    // Update the current time label
    public synchronized void setCurrentTime(AtomicInteger currentTime) {
        timeLabel.setText("Current Time: " + currentTime.get());
    }
}
