package com.analyzer.sysanalyzer;

import com.analyzer.sysanalyzer.adapters.KubernetesAdapter;
import com.analyzer.sysanalyzer.analyzers.Analyzer;
import com.analyzer.sysanalyzer.analyzers.AnalyzerTest;
import com.analyzer.sysanalyzer.analyzers.ResourcesProperties;
import com.analyzer.sysanalyzer.exceptions.ClusterErrorException;
import com.analyzer.sysanalyzer.services.MailService;
import com.analyzer.sysanalyzer.states.CommandEnum;
import com.analyzer.sysanalyzer.states.StateMachine;
import org.junit.Test;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


public class MicrobenchmarkTest {

    @State(Scope.Thread)
    public static class StaticStateMachine {
        private KubernetesAdapter adapter;
        private Analyzer analyzer;

        @Setup(Level.Invocation)
        public void initStateMachine() {
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
    }

    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void applyStrategyBenchmark_Increase_Fast(StaticStateMachine staticStateMachine) throws ClusterErrorException {
        Mockito.when(staticStateMachine.adapter.getCacheStatistics()).thenReturn(AnalyzerTest.generateClusterStatisticsForIncrease());
        staticStateMachine.analyzer.analyze();
        Mockito.verify(staticStateMachine.adapter).increaseService("accountService");
    }

    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void applyStrategyBenchmark_Decrease_Fast(StaticStateMachine staticStateMachine) throws ClusterErrorException {
        Mockito.when(staticStateMachine.adapter.getCacheStatistics()).thenReturn(AnalyzerTest.generateClusterStatisticsForDecrease());
        staticStateMachine.analyzer.analyze();
        Mockito.verify(staticStateMachine.adapter).decreaseService("salesService");
    }

    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void applyStrategyBenchmark_DecreaseWithNullableLoad_Fast(StaticStateMachine staticStateMachine) throws ClusterErrorException {
        Mockito.when(staticStateMachine.adapter.getCacheStatistics()).thenReturn(AnalyzerTest.generateClusterStatisticsForNullableLoad());
        staticStateMachine.analyzer.analyze();
        Mockito.verify(staticStateMachine.adapter).decreaseService("salesService");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MicrobenchmarkTest.class.getSimpleName())
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
