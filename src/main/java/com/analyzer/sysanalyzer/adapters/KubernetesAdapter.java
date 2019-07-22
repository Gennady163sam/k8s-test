package com.analyzer.sysanalyzer.adapters;

import com.analyzer.sysanalyzer.exceptions.ClusterErrorException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("kubernetesAdapter")
public class KubernetesAdapter implements ExternalSystemAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesAdapter.class);
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String MEMORY_ATTRIBUTE = "memory";
    private static final String TEST_DEPLOYMENT_NAME = "docker-demo";
    private CoreV1Api api;
    private ExtensionsV1beta1Api extensionV1Api;

    @Value("${adapter.access.token}")
    private String accessToken;
    @Value("${adapter.server.host}")
    private String serverHost;
    @Value("${adapter.server.port}")
    private String serverPort;

    private ClusterStatistics currentStatistics = new ClusterStatistics();
    private Exception errorGetData;

    @PostConstruct
    public void init() {
        ApiClient client = Config.fromToken("https://" + serverHost +":" + serverPort, accessToken);
        client.setVerifyingSsl(false);
        Configuration.setDefaultApiClient(client);
        api = new CoreV1Api(client);
        extensionV1Api = new ExtensionsV1beta1Api();
        extensionV1Api.setApiClient(api.getApiClient());

        updateCacheStatistics();
    }

    @Override
    public void updateCacheStatistics() {
        try {
            V1PodList kubePodes = this.api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
            kubePodes.getItems().forEach(item -> {
                ClusterNodeStatistics nodeStatistics;
                try {
                    nodeStatistics = convertNodeStatisticFromPod(item);
                    if (nodeStatistics != null) {
                        currentStatistics.updateNodeStatistic(nodeStatistics);
                    }
                } catch (ApiException e) {
                    errorGetData = e;
                }
            });
            currentStatistics.enrichNodesStatistic();
        } catch (ApiException e) {
            errorGetData = e;
        }
    }

    @Override
    public ClusterStatistics getCacheStatistics() throws ClusterErrorException {
        if (errorGetData != null) {
            Exception error = errorGetData;
            errorGetData = null;
            throw new ClusterErrorException(error);
        }
        return currentStatistics;
    }

    @Override
    public void increaseService(String serviceName) {
        try {
            LOGGER.debug("Increase service {}.", serviceName);
            String deploymentName = calcDeploymentNameByServiceName(serviceName);
            int currentCountReplicas = getCurrentDeployReplicas(deploymentName);
            scaleDeployment(deploymentName, currentCountReplicas + 1);
        } catch (ApiException e) {
            errorGetData = e;
        }
    }

    @Override
    public void decreaseService(String serviceName) {
        try {
            LOGGER.debug("Decrease service {}", serviceName);
            String deploymentName = calcDeploymentNameByServiceName(serviceName);
            int currentCountReplicas = getCurrentDeployReplicas(deploymentName);
            if (currentCountReplicas > 1) {
                scaleDeployment(deploymentName, currentCountReplicas - 1);
            }
        } catch (ApiException e) {
            errorGetData = e;
        }
    }

    private ClusterNodeStatistics convertNodeStatisticFromPod(V1Pod pod) throws ApiException {
        if (pod.getSpec().getNodeName() != null) {
            V1Node node = api.readNode(pod.getSpec().getNodeName(), null, null, null);
            ClusterNodeStatistics nodeStatistics = buildNodeStatisticFromNode(node);

            pod.getSpec().getContainers().forEach(c ->
                nodeStatistics.updateServiceStatistic(convertServiceStatistic(pod.getMetadata().getName(), c, nodeStatistics))
            );

            return nodeStatistics;
        }

        return null;
    }

    private ServiceStatistics convertServiceStatistic(String serviceName, V1Container container, ClusterNodeStatistics node) {
        ServiceStatistics serviceStatistics = new ServiceStatistics();
        serviceStatistics.setServiceName(serviceName);
        V1ResourceRequirements resources = container.getResources();

        if (resources != null) {
            Map<String, Quantity> requests = resources.getRequests();
            if (requests != null) {
                if (requests.get("cpu") != null) {
                    BigDecimal cpuValue = requests.get("cpu").getNumber();
                    Double cpuCapacity = node.getCpuCapacity();
                    serviceStatistics.setCpuRequestsPercent(cpuValue.doubleValue() / cpuCapacity * 100);
                    serviceStatistics.setCpuRequests(cpuValue.doubleValue());
                }
                if (requests.get(MEMORY_ATTRIBUTE) != null) {
                    BigDecimal memoryValue = requests.get(MEMORY_ATTRIBUTE).getNumber();
                    Double memoryCapacity = node.getMemoryCapacity();
                    serviceStatistics.setMemoryRequestsPercent(memoryValue.doubleValue()  / 1024 / 1024 / memoryCapacity * 100);
                    serviceStatistics.setMemoryRequests(memoryValue.doubleValue() / 1024 / 1024);
                }
            }
        }

        return serviceStatistics;
    }

    private ClusterNodeStatistics buildNodeStatisticFromNode(V1Node node) {
        if (currentStatistics.getNodeByName(node.getMetadata().getName()) != null) {
            return currentStatistics.getNodeByName(node.getMetadata().getName());
        }

        ClusterNodeStatistics clusterNodeStatistics = new ClusterNodeStatistics();
        clusterNodeStatistics.setNodeName(node.getMetadata().getName());

        Map<String, Quantity> nodeResources = node.getStatus().getCapacity();
        if (nodeResources.get(MEMORY_ATTRIBUTE) != null) {
            BigDecimal totalMemory = nodeResources.get(MEMORY_ATTRIBUTE).getNumber();
            clusterNodeStatistics.setMemoryCapacity(totalMemory.doubleValue() / 1024 / 1024);
        } else {
            clusterNodeStatistics.setMemoryCapacity(0.0);
        }
        if (nodeResources.get("cpu") != null) {
            BigDecimal totalCpu = nodeResources.get("cpu").getNumber();
            clusterNodeStatistics.setCpuCapacity(totalCpu.doubleValue());
        } else {
            clusterNodeStatistics.setCpuCapacity(0.0);
        }

        return clusterNodeStatistics;
    }

    /**
     * Scale up/down the number of pod in Deployment
     *
     * @param deploymentName - name of deployment
     * @param numberOfReplicas - required number of replicas deployment
     */
    private void scaleDeployment(String deploymentName, int numberOfReplicas)
            throws ApiException {
        Optional<ExtensionsV1beta1Deployment> deployment = getDeployment(deploymentName);

        deployment.ifPresent( deploy -> {
            try {
                ExtensionsV1beta1DeploymentSpec newSpec = deploy.getSpec().replicas(numberOfReplicas);
                ExtensionsV1beta1Deployment newDeploy = deploy.spec(newSpec);
                extensionV1Api.replaceNamespacedDeployment(
                        deploymentName, DEFAULT_NAMESPACE, newDeploy, null, null);
            } catch (ApiException ex) {
                LOGGER.warn("Scale the pod failed for Deployment:" + deploymentName, ex);
            }
        });
    }

    private int getCurrentDeployReplicas(String deploymentName) throws ApiException {
        Optional<ExtensionsV1beta1Deployment> deployment = getDeployment(deploymentName);

        if (deployment.isPresent()) {
            return deployment.get().getSpec().getReplicas();
        }
        return 0;
    }

    private Optional<ExtensionsV1beta1Deployment> getDeployment(String deploymentName)
            throws ApiException {
        ExtensionsV1beta1DeploymentList listNamespacedDeployment =
                extensionV1Api.listNamespacedDeployment(
                        DEFAULT_NAMESPACE, null, null, null, null, null, null, null, null, Boolean.FALSE);

        List<ExtensionsV1beta1Deployment> extensionsV1beta1DeploymentItems =
                listNamespacedDeployment.getItems();

        return extensionsV1beta1DeploymentItems
                        .stream()
                        .filter(
                                (ExtensionsV1beta1Deployment deployment) ->
                                        deployment.getMetadata().getName().equals(deploymentName))
                        .findFirst();
    }

    private String calcDeploymentNameByServiceName(String serviceName) {
        //TODO: Using serviceName
        return TEST_DEPLOYMENT_NAME;
    }
}
