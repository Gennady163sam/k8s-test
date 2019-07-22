package com.analyzer.sysanalyzer.states;

import com.analyzer.sysanalyzer.adapters.KubernetesAdapter;
import com.analyzer.sysanalyzer.services.MailService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;

import static com.analyzer.sysanalyzer.states.CommandEnum.*;
import static com.analyzer.sysanalyzer.states.StateEnum.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class StateMachineTest {
    private static String DEFAULT_SERVICE_NAME = "AppleService";
    private StateMachine stateMachine;
    private KubernetesAdapter adapter;
    private MailService mailService;

    @Before
    public void initStateMachine() {
        adapter = Mockito.mock(KubernetesAdapter.class);
        mailService = Mockito.mock(MailService.class);
        stateMachine = new StateMachine(adapter, mailService);
        stateMachine.init();
    }

    @Test
    public void checkAfterInitMethod_Ok() {
        Assert.assertEquals("State after init must be OFF", stateMachine.getCurrentState(), OFF.name());
    }

    @Test
    public void applyStrategy_TurnOn_Ok() {
        stateMachine.changeState(TURN_ON);
        stateMachine.applyStrategy();
        Assert.assertEquals("State must be ANALYZING after TURN_ON", stateMachine.getCurrentState(), ANALYZING.name());
        Mockito.verify(adapter, Mockito.times(2)).updateCacheStatistics();
    }

    @Test
    public void applyStrategy_Increase_Ok() {
        stateMachine.changeState(TURN_ON);
        StateMachineContext context = new StateMachineContext();
        context.setCommand(INCREASE);
        context.setServiceName(DEFAULT_SERVICE_NAME);
        stateMachine.changeState(context);
        Assert.assertEquals("State must be INCREASING after INCREASE", stateMachine.getCurrentState(), INCREASING.name());
        Mockito.verify(adapter).increaseService(DEFAULT_SERVICE_NAME);
    }

    @Test
    public void applyStrategy_Decrease_Ok() {
        stateMachine.changeState(TURN_ON);
        StateMachineContext context = new StateMachineContext();
        context.setCommand(DECREASE);
        context.setServiceName(DEFAULT_SERVICE_NAME);
        stateMachine.changeState(context);
        Assert.assertEquals("State must be DECREASING after DECREASE", stateMachine.getCurrentState(), DECREASING.name());
        Mockito.verify(adapter).decreaseService(DEFAULT_SERVICE_NAME);
    }

    @Test
    public void applyStrategy_Reconfiguring_Ok() {
        stateMachine.changeState(TURN_ON);
        stateMachine.changeState(RECONFIGURE);
        Assert.assertEquals("State must be RECONFIGURING after RECONFIGURE", stateMachine.getCurrentState(), RECONFIGURING.name());
        // TODO: Reconfigure method
    }

    @Test
    public void applyStrategy_NotifyingWithMessage_Ok() throws MessagingException {
        String errorText = "Test error notification text.";
        stateMachine.changeState(TURN_ON);
        stateMachine.pushError(errorText);
        stateMachine.changeState(ALERT);
        Assert.assertEquals("State must be NOTIFYING after ALERT", stateMachine.getCurrentState(), NOTIFYING.name());
        Mockito.verify(mailService).sendMessage("Error in the cluster zone!", errorText);
    }

    @Test
    public void applyStrategy_NotifyingWithoutMessage_Ok() {
        stateMachine.changeState(TURN_ON);
        stateMachine.changeState(ALERT);
        Assert.assertEquals("State must be NOTIFYING after ALERT", stateMachine.getCurrentState(), NOTIFYING.name());
    }

    @Test
    public void applyStrategy_TurnOff_Ok() {
        stateMachine.changeState(TURN_ON);
        stateMachine.changeState(TURN_OFF);
        Assert.assertEquals("State must be OFF after TURN_OFF", stateMachine.getCurrentState(), OFF.name());
    }

    @Test
    public void applyStrategy_Reconfigure_Ok() {
        stateMachine.changeState(TURN_ON);
        stateMachine.reconfigure();
        Assert.assertEquals("State must be RECONFIGURING after start reconfigure", stateMachine.getCurrentState(), RECONFIGURING.name());
    }
}