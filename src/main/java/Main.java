import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Suvorov
 */
public class Main {
    public static void main(String[] args) {
        // Create nodes
        Node node1 = new Node();
        Node node2 = new Node();

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

        // Create ADMI instance
        AdaptiveDistributedMultiLevelIndex admi = new AdaptiveDistributedMultiLevelIndex();

        // Create global and local indexes
        admi.createGlobalIndex(Arrays.asList(node1, node2));
        admi.createLocalIndex(node1);
        admi.createLocalIndex(node2);

        // Create a query
        Query query = new Query();
        query.targetData = 20;

        // Process query
        long start = System.nanoTime();
        List<Response> responses = admi.queryProcessing(query);
        long end = System.nanoTime();
        System.out.println("Query Processing took " + (end - start) + " nanoseconds");

        for (Response response : responses) {
            System.out.println("Received data: " + response.receivedData);
        }

        // Create metrics
        Metrics metrics = new Metrics();
        metrics.queryLoad = 110; // a value greater than 100 to trigger change
        metrics.systemLoad = 50;

        // Adapt indexing and partitioning
        start = System.nanoTime();
        admi.adaptIndexing(node1, metrics);
        admi.adaptPartitioning(Arrays.asList(node1, node2), metrics);
        end = System.nanoTime();
        System.out.println("Indexing and Partitioning took " + (end - start) + " nanoseconds");
    }
}
