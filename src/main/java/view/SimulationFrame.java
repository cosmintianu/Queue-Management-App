package view;

import model.SelectionPolicy;
import model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationFrame extends JFrame {
    private JPanel mainPanel;
    private JPanel outputPanel;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JLabel timeLabel;
    private JPanel inputPanel;
    private JTextField maxSimulationTimeField;

    private JTextField numServersField;
    private JTextField numTasksField;
    private JTextField minArrivalTimeField;
    private JTextField maxArrivalTimeField;
    private JTextField minServiceTimeField;
    private JTextField maxServiceTimeField;
    private JComboBox<String> strategyComboBox;
    private JButton startSimulationButton;
    private SimulationManagerListener simulationManagerListener;



    public SimulationFrame(SimulationManagerListener simulationManagerListener) {
        this.simulationManagerListener=simulationManagerListener;

        setTitle("Queue Management App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);

        // Create a panel to hold the time label
        inputPanel = new JPanel(new GridLayout(3, 10));
        initComponents();
        add(inputPanel, BorderLayout.NORTH);

        // Create a text area to display the queue status
        textArea = new JTextArea();
        textArea.setEditable(false); // Make it non-editable
        scrollPane = new JScrollPane(textArea); // Add scroll pane

        mainPanel = new JPanel(new BorderLayout()); // Use BorderLayout for mainPanel
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Add scroll pane to the center
        add(mainPanel, BorderLayout.CENTER); // Add mainPanel to the center

        setVisible(true);
    }

    // Update the text area with the tasks in the queues for all servers
    public synchronized void updateSimulationInfo(String info) {
        textArea.append(info + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength()); // Scroll to the bottom
    }

    // Update the current time label
    private void initComponents() {

        maxSimulationTimeField = new JTextField();
        maxSimulationTimeField.setText("60");
        inputPanel.add(new JLabel("Max Simulation Time:"));
        inputPanel.add(maxSimulationTimeField);

        numServersField = new JTextField();
        numServersField.setText("5");
        inputPanel.add(new JLabel("Number of Servers:"));
        inputPanel.add(numServersField);

        numTasksField = new JTextField();
        numTasksField.setText("50");
        inputPanel.add(new JLabel("Number of Tasks:"));
        inputPanel.add(numTasksField);

        minArrivalTimeField = new JTextField();
        minArrivalTimeField.setText("2");
        inputPanel.add(new JLabel("Min Arrival Time:"));
        inputPanel.add(minArrivalTimeField);

        maxArrivalTimeField = new JTextField();
        maxArrivalTimeField.setText("40");
        inputPanel.add(new JLabel("Max Arrival Time:"));
        inputPanel.add(maxArrivalTimeField);

        minServiceTimeField = new JTextField();
        minServiceTimeField.setText("1");
        inputPanel.add(new JLabel("Min Service Time:"));
        inputPanel.add(minServiceTimeField);

        maxServiceTimeField = new JTextField();
        maxServiceTimeField.setText("7");
        inputPanel.add(new JLabel("Max Service Time:"));
        inputPanel.add(maxServiceTimeField);

        String[] strategies = {"Shortest Time", "Shortest Queue"};
        strategyComboBox = new JComboBox<>(strategies);
        inputPanel.add(new JLabel("Strategy:"));
        inputPanel.add(strategyComboBox);

        JLabel emptyLabel = new JLabel();
        inputPanel.add(emptyLabel);

        startSimulationButton = new JButton("Start Simulation");
        startSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve text from text fields and parse into appropriate data types
                int maxSimulationTime = Integer.parseInt(maxSimulationTimeField.getText());
                int numServers = Integer.parseInt(numServersField.getText());
                int numTasks = Integer.parseInt(numTasksField.getText());
                int minArrivalTime = Integer.parseInt(minArrivalTimeField.getText());
                int maxArrivalTime = Integer.parseInt(maxArrivalTimeField.getText());
                int minServiceTime = Integer.parseInt(minServiceTimeField.getText());
                int maxServiceTime = Integer.parseInt(maxServiceTimeField.getText());

                SelectionPolicy selectionPolicy = strategyComboBox.getSelectedIndex() == 0 ?
                        SelectionPolicy.SHORTEST_TIME : SelectionPolicy.SHORTEST_QUEUE;
                // Pass the parsed values to the corresponding fields of SimulationManager
                simulationManagerListener.startSimulation(
                        maxSimulationTime, numServers, numTasks, minArrivalTime, maxArrivalTime,
                        minServiceTime, maxServiceTime,selectionPolicy
                );
            }
        });
        inputPanel.add(startSimulationButton);

        add(inputPanel);
    }
}
