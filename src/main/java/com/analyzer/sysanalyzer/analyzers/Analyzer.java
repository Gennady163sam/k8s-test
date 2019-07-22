package com.analyzer.sysanalyzer.analyzers;

import com.analyzer.sysanalyzer.adapters.ClusterNodeStatistics;
import com.analyzer.sysanalyzer.adapters.ClusterStatistics;
import com.analyzer.sysanalyzer.adapters.ExternalSystemAdapter;
import com.analyzer.sysanalyzer.adapters.ServiceStatistics;
import com.analyzer.sysanalyzer.exceptions.ClusterErrorException;
import com.analyzer.sysanalyzer.states.CommandEnum;
import com.analyzer.sysanalyzer.states.StateMachine;
import com.analyzer.sysanalyzer.states.StateMachineContext;
import com.google.common.collect.EvictingQueue;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component
public class Analyzer {
    private ExternalSystemAdapter adapter;
    private StateMachine stateMachine;
    private ResourcesProperties resourcesProperties;
    private Map<String,EvictingQueue<Double>> cpuValues = new HashMap<>();
    private Map<String,EvictingQueue<Double>> memoryValues = new HashMap<>();
    private ClusterStatistics lastStatistics;

    @Autowired
    public Analyzer(@Qualifier("kubernetesAdapter") ExternalSystemAdapter adapter, StateMachine stateMachine, ResourcesProperties resourcesProperties) {
        this.adapter = adapter;
        this.stateMachine = stateMachine;
        this.resourcesProperties = resourcesProperties;
    }

    @Scheduled(fixedRateString = "${analyzer.fixedRate.milliseconds}")
    public void analyze() {
        if (resourcesProperties.isAnalyzeEnable()) {
            ClusterStatistics statistics;
            try {
                statistics = adapter.getCacheStatistics();
                statistics.getNodes().forEach(n -> {
                    AnalyzeSolution result = calcStateByStatistics(n);
                    stateMachine.changeState(new StateMachineContext(result));
                });
                lastStatistics = statistics;
            } catch (ClusterErrorException ex) {
                String trace = ExceptionUtils.getStackTrace(ex);
                stateMachine.pushError(trace);
                stateMachine.changeState(CommandEnum.ALERT);
            }
        }
    }

    public ClusterStatistics getLastStatistics() {
        return lastStatistics;
    }

    public Map<String, EvictingQueue<Double>> getAllAverageCpu() {
        return this.cpuValues;
    }

    public Map<String, EvictingQueue<Double>> getAllAverageMemory() {
        return this.memoryValues;
    }

    public EvictingQueue<Double> getAverageCpuByNodeName(String nodeName) {
        return this.cpuValues.get(nodeName);
    }

    public EvictingQueue<Double> getAverageMemoryByNodeName(String nodeName) {
        return this.memoryValues.get(nodeName);
    }

    private AnalyzeSolution calcStateByStatistics(ClusterNodeStatistics statistics) {
        Double averageCpu = calcAndUpdateAverageCpu(statistics.getCpuRequestsPercent(), statistics.getNodeName());
        Double averageMemory = calcAndUpdateAverageMemory(statistics.getMemoryRequestsPercent(), statistics.getNodeName());

        boolean isNeedMoreResources = averageCpu.compareTo(resourcesProperties.getCpuPercentMax()) > 0 ||
                averageMemory.compareTo(resourcesProperties.getMemoryPercentMax()) > 0;

        boolean isNeedLessResources = averageCpu.compareTo(resourcesProperties.getCpuPercentMin()) < 0 &&
                averageMemory.compareTo(resourcesProperties.getMemoryPercentMin()) < 0;

        if (isNeedMoreResources) {
            return new AnalyzeSolution(CommandEnum.INCREASE, getServiceNameWithMaxLoad(statistics));
        }

        if (isNeedLessResources) {
            return new AnalyzeSolution(CommandEnum.DECREASE, getServiceNameWithMinLoad(statistics));
        }

        String serviceNameWithNullLoad = getServiceNameWithNullLoad(statistics);

        if (serviceNameWithNullLoad != null) {
            return new AnalyzeSolution(CommandEnum.DECREASE, serviceNameWithNullLoad);
        } else {
            return new AnalyzeSolution(CommandEnum.WAIT);
        }
    }

    private String getServiceNameWithMaxLoad(ClusterNodeStatistics statistics) {
        ServiceStatistics foundNode = statistics.getServices().stream().max(Comparator.comparing(ServiceStatistics::getCpuRequestsPercent)).orElse(null);
        return foundNode != null ? foundNode.getServiceName() : null;
    }

    private String getServiceNameWithMinLoad(ClusterNodeStatistics statistics) {
        ServiceStatistics foundNode = statistics.getServices().stream().min(Comparator.comparing(ServiceStatistics::getCpuRequestsPercent)).orElse(null);
        return foundNode != null ? foundNode.getServiceName() : null;
    }

    private String getServiceNameWithNullLoad(ClusterNodeStatistics statistics) {
        return statistics.getServices().stream().filter(s -> s.getCpuRequestsPercent().equals(0.0)).findFirst().orElse(new ServiceStatistics()).getServiceName();
    }

    private Double calcAndUpdateAverageCpu(Double currentCpu, String nodeName) {
        cpuValues.computeIfAbsent(nodeName, k -> EvictingQueue.create(10));
        cpuValues.get(nodeName).add(currentCpu);
        return cpuValues.get(nodeName).stream().mapToDouble(Double::doubleValue).sum() / cpuValues.get(nodeName).size();
    }

    private Double calcAndUpdateAverageMemory(Double currentMemory, String nodeName) {
        memoryValues.computeIfAbsent(nodeName, k -> EvictingQueue.create(3));
        memoryValues.get(nodeName).add(currentMemory);
        return memoryValues.get(nodeName).stream().mapToDouble(Double::doubleValue).sum() / memoryValues.get(nodeName).size();
    }
}
