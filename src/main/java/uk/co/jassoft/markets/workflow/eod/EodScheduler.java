package uk.co.jassoft.markets.workflow.eod;

import uk.co.jassoft.markets.datamodel.eod.EodType;
import uk.co.jassoft.markets.workflow.AbstractDockerScheduler;
import com.spotify.docker.client.DockerException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jonshaw on 01/09/2016.
 */
@Component
public class EodScheduler extends AbstractDockerScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(EodScheduler.class);

    @Value("${EOD_DATA_WEB_SERVICE}")
    private String eodDataWebService;

    @Value("${EOD_DATA_WEB_SERVICE_USERNAME}")
    private String eodServiceUsername;

    @Value("${EOD_DATA_WEB_SERVICE_PASSWORD}")
    private String eodServicePassword;

    private final List<String> envs = new ArrayList<>();

    @PostConstruct
    private void setup() {
        envs.add("EOD_DATA_WEB_SERVICE=" + eodDataWebService);
        envs.add("EOD_DATA_WEB_SERVICE_USERNAME=" + eodServiceUsername);
        envs.add("EOD_DATA_WEB_SERVICE_PASSWORD=" + eodServicePassword);
    }

    @JmsListener(destination = "MissingQuoteData")
    public void onMissingQuoteDateMessage(Message message) throws JMSException {
        if (message instanceof ObjectMessage) {
            final ObjectMessage objectMessage = (ObjectMessage) message;

            message.acknowledge();

            // TODO: Forbidden class org.apache.commons.lang3.tuple.ImmutablePair!
            // This class is not trusted to be serialized as ObjectMessage payload.
            // Please take a look at http://activemq.apache.org/objectmessage.html for more information on how to configure trusted classes.
            final Pair<String,Date> retrievalDate = (Pair<String,Date>) objectMessage.getObject();

            String exchange = retrievalDate.getLeft();

            long date = retrievalDate.getRight().getTime();

            scheduleContainer(EOD_IMAGE, envs, 300l, EodType.QuoteRetriever.name(), exchange, String.valueOf(date));
        }
    }

    @Scheduled(cron = "0 0 */2 * * ?")
    public void retrieveQuotes() {
        scheduleContainer(EOD_IMAGE, envs, 500l, EodType.QuoteRetriever.name());
    }

    @JmsListener(destination = "FindCompanies")
    public void onFindCompaniesMessage(Message message) throws DockerException, InterruptedException {
        LOG.info("Manually triggered find Companies");
        findCompanies();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void findCompanies() throws DockerException, InterruptedException {
        scheduleContainer(EOD_IMAGE, envs, 300l, EodType.CompanyFinder.name());
    }

}
