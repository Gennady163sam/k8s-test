package com.analyzer.sysanalyzer.adapters;

import com.analyzer.sysanalyzer.exceptions.ClusterErrorException;

public interface ExternalSystemAdapter {
    void updateCacheStatistics();
    ClusterStatistics getCacheStatistics() throws ClusterErrorException;
    void increaseService(String serviceName);
    void decreaseService(String serviceName);
}
