package com.analyzer.sysanalyzer.adapters;

public class ServiceStatistics {
    private Double cpuRequests;
    private Double cpuRequestsPercent;
    private Double memoryRequests;
    private Double memoryRequestsPercent;
    private String serviceName;

    public ServiceStatistics() {
        cpuRequests = 0.0;
        cpuRequestsPercent = 0.0;
        memoryRequests = 0.0;
        memoryRequestsPercent = 0.0;
    }

    public ServiceStatistics(Double cpuRequestsPercent, Double memoryRequestsPercent, String serviceName) {
        this.cpuRequestsPercent = cpuRequestsPercent;
        this.memoryRequestsPercent = memoryRequestsPercent;
        this.serviceName = serviceName;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
