/**
 * @author Igor Suvorov
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public class DistributedSystemGUI extends JFrame {
    private JTextField queryField;
    private JTextField metricsField;
    private JTextArea resultsArea;
    private AdaptiveDistributedMultiLevelIndex admi;
    private Node node1;
    private Node node2;

    public DistributedSystemGUI() {
        setTitle("Distributed System GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create nodes and partitions
        createNodesAndPartitions();

        // Create ADMI instance
        admi = new AdaptiveDistributedMultiLevelIndex();
        admi.createGlobalIndex(Arrays.asList(node1, node2));
        admi.createLocalIndex(node1);
        admi.createLocalIndex(node2);

        // Create the main menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        fileMenu.add(refreshItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create the query bar
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryField = new JTextField();
        JButton queryButton = new JButton("Query");
        queryPanel.add(new JLabel("Enter Query Target: "), BorderLayout.WEST);
        queryPanel.add(queryField, BorderLayout.CENTER);
        queryPanel.add(queryButton, BorderLayout.EAST);

        // Create the metrics bar
        JPanel metricsPanel = new JPanel(new BorderLayout());
        metricsField = new JTextField();
        JButton metricsButton = new JButton("Adapt");
        metricsPanel.add(new JLabel("Enter Metrics Load: "), BorderLayout.WEST);
        metricsPanel.add(metricsField, BorderLayout.CENTER);
        metricsPanel.add(metricsButton, BorderLayout.EAST);

        // Create the results display
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);

        // Add components to the frame
        add(queryPanel, BorderLayout.NORTH);
        add(metricsPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Action listener for the query button
        queryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String targetData = queryField.getText();
                // Perform search and update the results area
                Query query = new Query();
                query.targetData = Integer.parseInt(targetData);
                processQuery(query);
            }
        });

        // Action listener for the metrics button
        metricsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String queryLoad = metricsField.getText();
                // Perform adaptation and update the results area
                Metrics metrics = new Metrics();
                metrics.queryLoad = Integer.parseInt(queryLoad); // assuming queryLoad as integer
                metrics.systemLoad = 50; // assuming a static system load
                adaptIndexingAndPartitioning(metrics);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void createNodesAndPartitions() {
        // Create nodes
        node1 = new Node();
        node2 = new Node();

        // Create partitions
        Partition partition1 = new Partition();
        Partition partition2 = new Partition();
        Partition partition3 = new Partition();
        Partition partition4 = new Partition();

        // Assign data to partitions
        partition1.data = 10;
        partition2.data = 20;
        partition3.data = 30;
        partition4.data = 40;

        // Add partitions to nodes
        node1.dataPartitions = Arrays.asList(partition1, partition2);
        node2.dataPartitions = Arrays.asList(partition3, partition4);
    }

    private void processQuery(Query query) {
        long start = System.nanoTime();
        List<Response> responses = admi.queryProcessing(query);
        long end = System.nanoTime();
        String resultText = "Query Processing took " + (end - start) + " nanoseconds\n";
        for (Response response : responses) {
            resultText += "Received data: " + response.receivedData + "\n";
        }
        resultText += "Pruning power: " + admi.getPruningPower() + "\n";
        resultText += "Communication cost: " + admi.getCommunicationCost() + " nodes\n";
        resultsArea.setText(resultText);
        // Reset metrics after each query
        admi.resetMetrics();
    }

    private void adaptIndexingAndPartitioning(Metrics metrics) {
        long start = System.nanoTime();
        admi.adaptIndexing(node1, metrics);
        admi.adaptPartitioning(Arrays.asList(node1, node2), metrics);
        long end = System.nanoTime();
        resultsArea.setText("Indexing and Partitioning took " + (end - start) + " nanoseconds");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DistributedSystemGUI gui = new DistributedSystemGUI();
                gui.setVisible(true);
            }
        });
    }
}
