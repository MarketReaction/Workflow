package uk.co.jassoft.markets.workflow.sentiment;

import uk.co.jassoft.markets.workflow.AbstractDockerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jonshaw on 02/09/2016.
 */
@Component
public class SentimentScheduler extends AbstractDockerScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(SentimentScheduler.class);

    @JmsListener(destination = "MatchFound")
    public void onMessage(final Message message) throws JMSException {
        final Date start = new Date();
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;

            LOG.info("Triggering Senitment as MatchFound");

            scheduleContainer(SENTIMENT_IMAGE, new ArrayList<String>(), 100l, textMessage.getText());
        }
    }
}