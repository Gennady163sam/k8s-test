package com.analyzer.sysanalyzer.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterStatistics {
    private Map<String, ClusterNodeStatistics> nodes;

    public ClusterStatistics() {
        nodes = new HashMap<>();
    }

    public ClusterStatistics(List<ClusterNodeStatistics> nodeStatistics) {
        nodeStatistics.forEach(n -> nodes.put(n.getNodeName(), n));
    }

    public List<ClusterNodeStatistics> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public void setNodes(List<ClusterNodeStatistics> nodeStatistics) {
        nodeStatistics.forEach(n -> nodes.put(n.getNodeName(), n));
    }

    public void updateNodeStatistic(ClusterNodeStatistics nodeStatistic) {
        nodes.put(nodeStatistic.getNodeName(), nodeStatistic);
    }

    public void updateNodeServiceStatistic(String nodeName, ServiceStatistics serviceStatistics) {
        if (nodes.get(nodeName) != null) {
            nodes.get(nodeName).updateServiceStatistic(serviceStatistics);
        }
    }

    public void enrichNodesStatistic() {
        this.nodes.values().forEach(ClusterNodeStatistics::enrichNodeStatistic);
    }

    public ClusterNodeStatistics getNodeByName(String nodeName) {
        return this.nodes.get(nodeName);
    }
}
