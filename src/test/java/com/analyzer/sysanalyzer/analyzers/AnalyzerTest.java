package com.analyzer.sysanalyzer.analyzers;

import com.analyzer.sysanalyzer.adapters.*;
import com.analyzer.sysanalyzer.exceptions.ClusterErrorException;
import com.analyzer.sysanalyzer.services.MailService;
import com.analyzer.sysanalyzer.states.CommandEnum;
import com.analyzer.sysanalyzer.states.StateMachine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class AnalyzerTest {
    private KubernetesAdapter adapter;
    private Analyzer analyzer;

    @Before
    public void init() {
        adapter = Mockito.mock(KubernetesAdapter.class);
        MailService mailService = Mockito.mock(MailService.class);
        StateMachine stateMachine = new StateMachine(adapter, mailService);
        stateMachine.init();
        ResourcesProperties resourcesProperties = Mockito.mock(ResourcesProperties.class);
        stateMachine.changeState(CommandEnum.TURN_ON);
        analyzer = new Analyzer(adapter, stateMachine, resourcesProperties);
        Mockito.when(resourcesProperties.isAnalyzeEnable()).thenReturn(true);
        Mockito.when(resourcesProperties.getCpuPercentMin()).thenReturn(10.0);
        Mockito.when(resourcesProperties.getCpuPercentMax()).thenReturn(80.0);
        Mockito.when(resourcesProperties.getMemoryPercentMin()).thenReturn(2.0);
        Mockito.when(resourcesProperties.getMemoryPercentMax()).thenReturn(80.0);
    }

    @Test
    public void applyStrategy_IncreaseCluster_Ok() throws ClusterErrorException {
        Mockito.when(adapter.getCacheStatistics()).thenReturn(generateClusterStatisticsForIncrease());
        analyzer.analyze();
        Mockito.verify(adapter).increaseService("accountService");
    }

    @Test
    public void applyStrategy_DecreaseCluster_Ok() throws ClusterErrorException {
        Mockito.when(adapter.getCacheStatistics()).thenReturn(generateClusterStatisticsForDecrease());
        analyzer.analyze();
        Mockito.verify(adapter).decreaseService("salesService");
    }

    @Test
    public void applyStrategy_DecreaseClusterWithNullableLoad_Ok() throws ClusterErrorException {
        Mockito.when(adapter.getCacheStatistics()).thenReturn(generateClusterStatisticsForNullableLoad());
        analyzer.analyze();
        Mockito.verify(adapter).decreaseService("salesService");
    }

    @Test
    public void applyStrategy_AnalyzeCluster_Ok() throws ClusterErrorException {
        Mockito.when(adapter.getCacheStatistics()).thenReturn(generateGoodClusterStatistics());
        analyzer.analyze();
        // 2 nodes + initialize = 3 times invoking updateCahceStatistics
        Mockito.verify(adapter, Mockito.times(3)).updateCacheStatistics();
    }

    public static ClusterStatistics generateClusterStatisticsForIncrease() {
        ClusterStatistics clusterStatistics = new ClusterStatistics();
        List<ClusterNodeStatistics> clusterNodeStatistics = new ArrayList<>();

        ClusterNodeStatistics firstNode = new ClusterNodeStatistics();
        firstNode.setNodeName("Moscow_node");
        firstNode.setCpuRequests(95.5);
        firstNode.setCpuRequestsPercent(95.5);
        firstNode.setMemoryRequests(3345.0);
        firstNode.setMemoryRequestsPercent(65.0);

        List<ServiceStatistics> servicesFirstNode = new ArrayList<>();
        ServiceStatistics accountServiceFirstNode = new ServiceStatistics();
        accountServiceFirstNode.setCpuRequestsPercent(70.0);
        accountServiceFirstNode.setMemoryRequestsPercent(45.0);
        accountServiceFirstNode.setServiceName("accountService");
        servicesFirstNode.add(accountServiceFirstNode);

        ServiceStatistics salesServiceFirstNode = new ServiceStatistics();
        salesServiceFirstNode.setCpuRequestsPercent(25.5);
        salesServiceFirstNode.setMemoryRequestsPercent(20.0);
        salesServiceFirstNode.setServiceName("salesService");
        servicesFirstNode.add(salesServiceFirstNode);

        firstNode.setServices(servicesFirstNode);
        clusterNodeStatistics.add(firstNode);

        ClusterNodeStatistics secondNode = new ClusterNodeStatistics();
        secondNode.setNodeName("Samara_node");
        secondNode.setCpuRequests(50.0);
        secondNode.setCpuRequestsPercent(50.0);
        secondNode.setMemoryRequests(2345.0);
        secondNode.setMemoryRequestsPercent(35.0);

        List<ServiceStatistics> servicesSecondNode = new ArrayList<>();
        ServiceStatistics accountServiceSecondNode = new ServiceStatistics();
        accountServiceSecondNode.setCpuRequestsPercent(25.0);
        accountServiceSecondNode.setMemoryRequestsPercent(25.0);
        accountServiceSecondNode.setServiceName("accountService");
        servicesSecondNode.add(accountServiceSecondNode);

        ServiceStatistics salesServiceSecondNode = new ServiceStatistics();
        salesServiceSecondNode.setCpuRequestsPercent(25.0);
        salesServiceSecondNode.setMemoryRequestsPercent(10.0);
        salesServiceSecondNode.setServiceName("salesService");
        servicesSecondNode.add(salesServiceSecondNode);

        secondNode.setServices(servicesSecondNode);
        clusterNodeStatistics.add(secondNode);

        clusterStatistics.setNodes(clusterNodeStatistics);
        return clusterStatistics;
    }

    public static ClusterStatistics generateClusterStatisticsForDecrease() {
        ClusterStatistics clusterStatistics = new ClusterStatistics();
        List<ClusterNodeStatistics> clusterNodeStatistics = new ArrayList<>();

        ClusterNodeStatistics firstNode = new ClusterNodeStatistics();
        firstNode.setNodeName("Moscow_node");
        firstNode.setCpuRequests(35.0);
        firstNode.setCpuRequestsPercent(35.0);
        firstNode.setMemoryRequests(1345.0);
        firstNode.setMemoryRequestsPercent(25.0);

        List<ServiceStatistics> servicesFirstNode = new ArrayList<>();
        ServiceStatistics accountServiceFirstNode = new ServiceStatistics();
        accountServiceFirstNode.setCpuRequestsPercent(20.0);
        accountServiceFirstNode.setMemoryRequestsPercent(15.0);
        accountServiceFirstNode.setServiceName("accountService");
        servicesFirstNode.add(accountServiceFirstNode);

        ServiceStatistics salesServiceFirstNode = new ServiceStatistics();
        salesServiceFirstNode.setCpuRequestsPercent(15.0);
        salesServiceFirstNode.setMemoryRequestsPercent(10.0);
        salesServiceFirstNode.setServiceName("salesService");
        servicesFirstNode.add(salesServiceFirstNode);

        firstNode.setServices(servicesFirstNode);
        clusterNodeStatistics.add(firstNode);

        ClusterNodeStatistics secondNode = new ClusterNodeStatistics();
        secondNode.setNodeName("Samara_node");
        secondNode.setCpuRequests(5.0);
        secondNode.setCpuRequestsPercent(5.0);
        secondNode.setMemoryRequests(22.0);
        secondNode.setMemoryRequestsPercent(1.0);

        List<ServiceStatistics> servicesSecondNode = new ArrayList<>();
        ServiceStatistics accountServiceSecondNode = new ServiceStatistics();
        accountServiceSecondNode.setCpuRequestsPercent(4.0);
        accountServiceSecondNode.setMemoryRequestsPercent(1.0);
        accountServiceSecondNode.setServiceName("accountService");
        servicesSecondNode.add(accountServiceSecondNode);

        ServiceStatistics salesServiceSecondNode = new ServiceStatistics();
        salesServiceSecondNode.setCpuRequestsPercent(1.0);
        salesServiceSecondNode.setMemoryRequestsPercent(0.0);
        salesServiceSecondNode.setServiceName("salesService");
        servicesSecondNode.add(salesServiceSecondNode);

        secondNode.setServices(servicesSecondNode);
        clusterNodeStatistics.add(secondNode);

        clusterStatistics.setNodes(clusterNodeStatistics);
        return clusterStatistics;
    }

    public static ClusterStatistics generateClusterStatisticsForNullableLoad() {
        ClusterStatistics clusterStatistics = new ClusterStatistics();
        List<ClusterNodeStatistics> clusterNodeStatistics = new ArrayList<>();

        ClusterNodeStatistics firstNode = new ClusterNodeStatistics();
        firstNode.setNodeName("Moscow_node");
        firstNode.setCpuRequests(35.0);
        firstNode.setCpuRequestsPercent(35.0);
        firstNode.setMemoryRequests(1345.0);
        firstNode.setMemoryRequestsPercent(25.0);

        List<ServiceStatistics> servicesFirstNode = new ArrayList<>();
        ServiceStatistics accountServiceFirstNode = new ServiceStatistics();
        accountServiceFirstNode.setCpuRequestsPercent(20.0);
        accountServiceFirstNode.setMemoryRequestsPercent(15.0);
        accountServiceFirstNode.setServiceName("accountService");
        servicesFirstNode.add(accountServiceFirstNode);

        ServiceStatistics salesServiceFirstNode = new ServiceStatistics();
        salesServiceFirstNode.setCpuRequestsPercent(15.0);
        salesServiceFirstNode.setMemoryRequestsPercent(10.0);
        salesServiceFirstNode.setServiceName("salesService");
        servicesFirstNode.add(salesServiceFirstNode);

        firstNode.setServices(servicesFirstNode);
        clusterNodeStatistics.add(firstNode);

        ClusterNodeStatistics secondNode = new ClusterNodeStatistics();
        secondNode.setNodeName("Samara_node");
        secondNode.setCpuRequests(35.0);
        secondNode.setCpuRequestsPercent(35.0);
        secondNode.setMemoryRequests(2000.0);
        secondNode.setMemoryRequestsPercent(45.0);

        List<ServiceStatistics> servicesSecondNode = new ArrayList<>();
        ServiceStatistics accountServiceSecondNode = new ServiceStatistics();
        accountServiceSecondNode.setCpuRequestsPercent(35.0);
        accountServiceSecondNode.setMemoryRequestsPercent(45.0);
        accountServiceSecondNode.setServiceName("accountService");
        servicesSecondNode.add(accountServiceSecondNode);

        ServiceStatistics salesServiceSecondNode = new ServiceStatistics();
        salesServiceSecondNode.setCpuRequestsPercent(0.0);
        salesServiceSecondNode.setMemoryRequestsPercent(0.0);
        salesServiceSecondNode.setServiceName("salesService");
        servicesSecondNode.add(salesServiceSecondNode);

        secondNode.setServices(servicesSecondNode);
        clusterNodeStatistics.add(secondNode);

        clusterStatistics.setNodes(clusterNodeStatistics);
        return clusterStatistics;
    }

    private ClusterStatistics generateGoodClusterStatistics() {
        ClusterStatistics clusterStatistics = new ClusterStatistics();
        List<ClusterNodeStatistics> clusterNodeStatistics = new ArrayList<>();

        ClusterNodeStatistics firstNode = new ClusterNodeStatistics();
        firstNode.setNodeName("Moscow_node");
        firstNode.setCpuRequests(35.0);
        firstNode.setCpuRequestsPercent(35.0);
        firstNode.setMemoryRequests(1345.0);
        firstNode.setMemoryRequestsPercent(25.0);

        List<ServiceStatistics> servicesFirstNode = new ArrayList<>();
        ServiceStatistics accountServiceFirstNode = new ServiceStatistics();
        accountServiceFirstNode.setCpuRequestsPercent(20.0);
        accountServiceFirstNode.setMemoryRequestsPercent(15.0);
        accountServiceFirstNode.setServiceName("accountService");
        servicesFirstNode.add(accountServiceFirstNode);

        ServiceStatistics salesServiceFirstNode = new ServiceStatistics();
        salesServiceFirstNode.setCpuRequestsPercent(15.0);
        salesServiceFirstNode.setMemoryRequestsPercent(10.0);
        salesServiceFirstNode.setServiceName("salesService");
        servicesFirstNode.add(salesServiceFirstNode);

        firstNode.setServices(servicesFirstNode);
        clusterNodeStatistics.add(firstNode);

        ClusterNodeStatistics secondNode = new ClusterNodeStatistics();
        secondNode.setNodeName("Samara_node");
        secondNode.setCpuRequests(55.0);
        secondNode.setCpuRequestsPercent(55.0);
        secondNode.setMemoryRequests(3000.0);
        secondNode.setMemoryRequestsPercent(65.0);

        List<ServiceStatistics> servicesSecondNode = new ArrayList<>();
        ServiceStatistics accountServiceSecondNode = new ServiceStatistics();
        accountServiceSecondNode.setCpuRequestsPercent(35.0);
        accountServiceSecondNode.setMemoryRequestsPercent(45.0);
        accountServiceSecondNode.setServiceName("accountService");
        servicesSecondNode.add(accountServiceSecondNode);

        ServiceStatistics salesServiceSecondNode = new ServiceStatistics();
        salesServiceSecondNode.setCpuRequestsPercent(20.0);
        salesServiceSecondNode.setMemoryRequestsPercent(20.0);
        salesServiceSecondNode.setServiceName("salesService");
        servicesSecondNode.add(salesServiceSecondNode);

        secondNode.setServices(servicesSecondNode);
        clusterNodeStatistics.add(secondNode);

        clusterStatistics.setNodes(clusterNodeStatistics);
        return clusterStatistics;
    }
}