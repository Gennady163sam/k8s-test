package com.analyzer.sysanalyzer.states;

import com.analyzer.sysanalyzer.analyzers.AnalyzeSolution;
import com.analyzer.sysanalyzer.services.MailService;

import javax.mail.MessagingException;
import java.util.Deque;
import java.util.Objects;

public class StateMachineContext {
    private CommandEnum command;
    private String serviceName;
    private Deque<String> trace;
    private MailService mailService;

    public StateMachineContext() {}

    public StateMachineContext(AnalyzeSolution solution) {
        command = solution.getCommand();
        serviceName = solution.getServiceName();
    }

    public StateMachineContext(CommandEnum command, String serviceName, Deque<String> trace) {
        this.command = command;
        this.serviceName = serviceName;
        this.trace = trace;
    }

    public CommandEnum getCommand() {
        return command;
    }

    public void setCommand(CommandEnum command) {
        this.command = command;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Deque<String> getTrace() {
        return trace;
    }

    public void setTrace(Deque<String> trace) {
        this.trace = trace;
    }

    public void sendMessage(String head, String body) throws MessagingException {
        mailService.sendMessage(head, body);
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateMachineContext that = (StateMachineContext) o;
        return command == that.command &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(trace, that.trace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, serviceName, trace);
    }
}
