package uk.co.jassoft.markets.workflow.prediction;

import uk.co.jassoft.markets.datamodel.prediction.PredictorType;
import uk.co.jassoft.markets.workflow.AbstractDockerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import org.apache.activemq.command.ActiveMQBytesMessage;

/**
 * Created by jonshaw on 02/09/2016.
 */
@Component
public class PredictionScheduler extends AbstractDockerScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(PredictionScheduler.class);

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void validatePredictions() {
        scheduleContainer(PREDICTOR_IMAGE, new ArrayList<String>(), 100l, PredictorType.PredictionValidator.name());
    }

    @JmsListener(destination = "SentimentUpdated", concurrency = "5")
    public void onMessage(Message message) throws JMSException {

        if ( message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;

            message.acknowledge();

            scheduleContainer(PREDICTOR_IMAGE, new ArrayList<String>(), 100l, PredictorType.PredictionGenerator.name(), textMessage.getText());
        } else if ( message instanceof ActiveMQBytesMessage) {
            final ActiveMQBytesMessage bytesMessage = (ActiveMQBytesMessage) message;
            LOG.info("Message on Queue SentimentUpdated is not of type 'TextMessage' but is of type [{}] Test Content [{}]", message.getClass(), bytesMessage.readUTF());
        } else {
            LOG.info("Message on Queue SentimentUpdated is of unhandled type [{}]", message.getClass());
        }
    }

}
