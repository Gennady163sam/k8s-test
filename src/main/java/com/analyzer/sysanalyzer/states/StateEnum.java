package com.analyzer.sysanalyzer.states;

import com.analyzer.sysanalyzer.adapters.ExternalSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.util.NoSuchElementException;

public enum StateEnum {
    ANALYZING {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            adapter.updateCacheStatistics();
        }

        @Override
        public StateEnum change(CommandEnum command) {
            switch (command) {
                case INCREASE:
                    return INCREASING;
                case DECREASE:
                    return DECREASING;
                case RECONFIGURE:
                    return RECONFIGURING;
                case ALERT:
                    return NOTIFYING;
                case TURN_OFF:
                    return OFF;
                case WAIT:
                    return ANALYZING;
                default:
                    throw new UnsupportedOperationException(UNSUPPORTED_COMMAND + command.name());
            }
        }
    }, INCREASING {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            adapter.increaseService(context.getServiceName());
        }

        @Override
        public StateEnum change(CommandEnum command) {
            switch (command) {
                case WAIT:
                    return ANALYZING;
                case ALERT:
                    return NOTIFYING;
                case TURN_OFF:
                    return null;
                case INCREASE:
                    return INCREASING;
                default:
                    throw new UnsupportedOperationException(UNSUPPORTED_COMMAND + command.name());

            }
        }
    }, DECREASING {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            adapter.decreaseService(context.getServiceName());
        }

        @Override
        public StateEnum change(CommandEnum command) {
            switch (command) {
                case WAIT:
                    return ANALYZING;
                case ALERT:
                    return NOTIFYING;
                case DECREASE:
                    return DECREASING;
                case TURN_OFF:
                    return null;
                default:
                    throw new UnsupportedOperationException(UNSUPPORTED_COMMAND + command.name());
            }
        }
    }, RECONFIGURING {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            //TODO: realize configure method
        }

        @Override
        public StateEnum change(CommandEnum command) {
            switch (command) {
                case WAIT:
                    return ANALYZING;
                case ALERT:
                    return NOTIFYING;
                case RECONFIGURE:
                    return RECONFIGURING;
                default:
                    throw new UnsupportedOperationException(UNSUPPORTED_COMMAND + command.name());
            }
        }
    }, NOTIFYING {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            try {
                String message = context.getTrace().pop();
                if (message != null) {
                    logger.error(message);
                    context.sendMessage("Error in the cluster zone!", message);
                }
            } catch (NoSuchElementException ex) {
                logger.debug("Queue errors is empty!");
            } catch (MessagingException e) {
                logger.error("Error sending notify message!", e);
            }
        }

        @Override
        public StateEnum change(CommandEnum command) {
            switch (command) {
                case WAIT:
                    return ANALYZING;
                case TURN_OFF:
                    return null;
                case ALERT:
                    return NOTIFYING;
                default:
                    throw new UnsupportedOperationException(UNSUPPORTED_COMMAND + command.name());
            }
        }
    },
    OFF {
        @Override
        public void handle(ExternalSystemAdapter adapter, StateMachineContext context) {
            // do nothing
        }

        @Override
        public StateEnum change(CommandEnum command) {
            if (command == CommandEnum.TURN_ON) {
                return ANALYZING;
            }
            return OFF;
        }
    };

    private static final String UNSUPPORTED_COMMAND = "Unsupported command - ";
    private static final Logger logger = LoggerFactory.getLogger(StateEnum.class);
    public abstract void handle(ExternalSystemAdapter adapter, StateMachineContext context);
    public abstract StateEnum change(CommandEnum command);
}
