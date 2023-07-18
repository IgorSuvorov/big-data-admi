import java.util.*;

/**
 * @author Igor Suvorov
 */
public class AdaptiveDistributedMultiLevelIndex {
    Map<Node, Summary> globalIndex = new HashMap<>();
    Map<Node, Map<Partition, Integer>> localIndexes = new HashMap<>();

    public Summary summarize(List<Partition> nodeData) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Partition partition : nodeData) {
            min = Math.min(min, partition.data);
            max = Math.max(max, partition.data);
        }
        Summary summary = new Summary();
        summary.minData = min;
        summary.maxData = max;
        return summary;
    }

    public void createGlobalIndex(List<Node> distributedSystem) {
        for (Node node : distributedSystem) {
            List<Partition> nodeData = node.dataPartitions;
            globalIndex.put(node, summarize(nodeData));
        }
    }

    public void createLocalIndex(Node node) {
        List<Partition> dataPartitions = node.dataPartitions;
        Map<Partition, Integer> localIndex = new HashMap<>();
        for (Partition partition : dataPartitions) {
            localIndex.put(partition, index(partition));
        }
        localIndexes.put(node, localIndex);
    }

    public List<Node> queryRouting(Query query) {
        List<Node> relevantNodes = new ArrayList<>();
        for (Map.Entry<Node, Summary> entry : globalIndex.entrySet()) {
            Node node = entry.getKey();
            Summary summary = entry.getValue();
            if (queryMatchesSummary(query, summary)) {
                relevantNodes.add(node);
            }
        }
        return relevantNodes;
    }

    public List<Node> loadBalancing(Query query, List<Node> relevantNodes) {
        Map<Node, Integer> workloadDistribution = new HashMap<>();
        for (Node node : relevantNodes) {
            workloadDistribution.put(node, estimateWorkload(node, query));
        }
        return balance(workloadDistribution);
    }

    public List<Response> queryProcessing(Query query) {
        List<Node> relevantNodes = queryRouting(query);
        List<Node> balancedNodes = loadBalancing(query, relevantNodes);
        List<Response> responses = new ArrayList<>();
        for (Node node : balancedNodes) {
            responses.add(sendQuery(node, query));
        }
        return responses;
    }

    public int index(Partition partition) {
        return partition.data;
    }

    public boolean queryMatchesSummary(Query query, Summary summary) {
        return query.targetData >= summary.minData && query.targetData <= summary.maxData;
    }

    public int estimateWorkload(Node node, Query query) {

        return Math.abs(query.targetData - node.dataPartitions.size());
    }

    public List<Node> balance(Map<Node, Integer> workloadDistribution) {

        List<Node> balancedNodes = new ArrayList<>(workloadDistribution.keySet());
        Collections.shuffle(balancedNodes);
        return balancedNodes;
    }

    public Response sendQuery(Node node, Query query) {

        for (Partition partition : node.dataPartitions) {
            if (partition.data == query.targetData) {
                Response response = new Response();
                response.receivedData = partition.data;
                return response;
            }
        }
        return null;
    }

    public void adaptIndexing(Node node, Metrics metrics) {
        if (metricsIndicateChange(metrics)) {
            createLocalIndex(node);
        }
    }

    public void adaptPartitioning(List<Node> distributedSystem, Metrics metrics) {
        if (metricsIndicateChange(metrics)) {
            createGlobalIndex(distributedSystem);
        }
    }

    public boolean metricsIndicateChange(Metrics metrics) {
        return metrics.queryLoad > 100 || metrics.systemLoad > 100;
    }

}
