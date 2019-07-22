package com.analyzer.sysanalyzer.analyzers;

import com.analyzer.sysanalyzer.states.CommandEnum;

import java.util.Objects;

public class AnalyzeSolution {
    private CommandEnum command;
    private String serviceName;

    public AnalyzeSolution(CommandEnum command) {
        this.command = command;
    }

    public AnalyzeSolution(CommandEnum command, String serviceName) {
        this.command = command;
        this.serviceName = serviceName;
    }

    public CommandEnum getCommand() {
        return command;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzeSolution that = (AnalyzeSolution) o;
        return command == that.command &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, serviceName);
    }
}
