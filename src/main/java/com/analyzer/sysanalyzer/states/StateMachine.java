package com.analyzer.sysanalyzer.states;

import com.analyzer.sysanalyzer.adapters.ExternalSystemAdapter;
import com.analyzer.sysanalyzer.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class StateMachine {
    private ExternalSystemAdapter adapter;
    private StateEnum state;
    private Deque<String> errors = new ArrayDeque<>();
    private StateMachineContext currentContext;
    private MailService mailService;

    @Autowired
    public StateMachine(@Qualifier("kubernetesAdapter") ExternalSystemAdapter adapter, MailService mailService) {
        this.adapter = adapter;
        this.mailService = mailService;
    }

    @PostConstruct
    public void init() {
        if (state == null) {
            state = StateEnum.OFF;
        }
    }

    public void applyStrategy() {
        if (currentContext == null) {
            currentContext = new StateMachineContext();
        }
        currentContext.setTrace(errors);
        currentContext.setMailService(this.mailService);
        if (!errors.isEmpty() && !getCurrentState().equals(StateEnum.NOTIFYING.name())) {
            changeState(CommandEnum.ALERT);
        }
        state.handle(this.adapter, currentContext);
    }

    public void changeState(CommandEnum command) {
        state = state.change(command);
        applyStrategy();
    }

    public void changeState(StateMachineContext context) {
        currentContext = context;
        changeState(context.getCommand());
    }

    public void pushError(String trace) {
        errors.push(trace);
    }

    public void reconfigure() {
        state = state.change(CommandEnum.RECONFIGURE);
    }

    public String getCurrentState() {
        return state.name();
    }
}