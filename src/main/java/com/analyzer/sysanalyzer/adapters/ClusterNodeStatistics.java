package com.analyzer.sysanalyzer.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterNodeStatistics {
    private Double cpuRequests;
    private Double cpuRequestsPercent;
    private Double memoryRequests;
    private Double memoryRequestsPercent;
    private Double memoryCapacity;
    private Double cpuCapacity;
    private String nodeName;
    private Map<String, ServiceStatistics> services;

    public ClusterNodeStatistics() {
        services  = new HashMap<>();
    }

    public ClusterNodeStatistics(Double cpuRequests, Double cpuRequestsPercent, Double memoryRequests, Double memoryRequestsPercent) {
        this.cpuRequests = cpuRequests;
        this.cpuRequestsPercent = cpuRequestsPercent;
        this.memoryRequests = memoryRequests;
        this.memoryRequestsPercent = memoryRequestsPercent;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<ServiceStatistics> getServices() {
        return new ArrayList<>(services.values());
    }

    public void setServices(List<ServiceStatistics> servicesStatistics) {
        servicesStatistics.forEach(s -> services.put(s.getServiceName(), s));
    }

    public Double getCpuRequests() {
        return cpuRequests;
    }

    public void setCpuRequests(Double cpuRequests) {
        this.cpuRequests = cpuRequests;
    }

    public Double getCpuRequestsPercent() {
        return cpuRequestsPercent;
    }

    public void setCpuRequestsPercent(Double cpuRequestsPercent) {
        this.cpuRequestsPercent = cpuRequestsPercent;
    }

    public Double getMemoryRequests() {
        return memoryRequests;
    }

    public void setMemoryRequests(Double memoryRequests) {
        this.memoryRequests = memoryRequests;
    }

    public Double getMemoryRequestsPercent() {
        return memoryRequestsPercent;
    }

    public void setMemoryRequestsPercent(Double memoryRequestsPercent) {
        this.memoryRequestsPercent = memoryRequestsPercent;
    }

    public Double getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(Double memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public Double getCpuCapacity() {
        return cpuCapacity;
    }

    public void setCpuCapacity(Double cpuCapacity) {
        this.cpuCapacity = cpuCapacity;
    }

    public void updateServiceStatistic(ServiceStatistics serviceStatistics) {
        services.put(serviceStatistics.getServiceName(), serviceStatistics);
    }

    public void enrichNodeStatistic() {
        Double allocatedMemory = services.values().stream().mapToDouble(ServiceStatistics::getMemoryRequests).sum();
        this.memoryRequests = allocatedMemory;
        this.memoryRequestsPercent = allocatedMemory / memoryCapacity * 100;

        Double allocatedCpu = services.values().stream().mapToDouble(ServiceStatistics::getCpuRequests).sum();
        this.cpuRequests = allocatedCpu;
        this.cpuRequestsPercent = allocatedCpu / cpuCapacity * 100;
    }
}
