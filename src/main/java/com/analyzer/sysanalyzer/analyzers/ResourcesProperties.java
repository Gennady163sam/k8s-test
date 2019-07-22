package com.analyzer.sysanalyzer.analyzers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ResourcesProperties {
    private Environment environment;
    private boolean analyzing;

    @Autowired
    public ResourcesProperties(Environment environment) {
        this.environment = environment;
    }

    private String getProperty(String property) {
        return environment.getProperty(property);
    }

    public Double getCpuPercentMin() {
        return new Double(getProperty("resources.free.cpu.percent.min"));
    }

    public Double getCpuPercentMax() {
        return new Double(getProperty("resources.free.cpu.percent.max"));
    }

    public Double getMemoryPercentMin() {
        return new Double(getProperty("resources.free.memory.percent.min"));
    }

    public Double getMemoryPercentMax() {
        return new Double(getProperty("resources.free.memory.percent.max"));
    }

    public boolean isAnalyzeEnable() {
        return analyzing;
    }

    public void setAnalyzing(boolean analyzing) {
        this.analyzing = analyzing;
    }
}
