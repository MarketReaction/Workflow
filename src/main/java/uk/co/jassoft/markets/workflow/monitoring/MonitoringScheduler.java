package uk.co.jassoft.markets.workflow.monitoring;

import uk.co.jassoft.markets.datamodel.monitoring.MonitoringType;
import uk.co.jassoft.markets.workflow.AbstractDockerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Created by jonshaw on 06/09/2016.
 */
@Component
public class MonitoringScheduler extends AbstractDockerScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringScheduler.class);

    @Scheduled(cron = "0 */10 * * * ?") // Every 10 Minutes
    public void sourceCrawlerMonitor() {
        scheduleContainer(MONITORING_IMAGE, new ArrayList<String>(), 100l, MonitoringType.SourceCrawler.name());
    }

    @Scheduled(cron = "0 */10 * * * ?") // Every 10 Minutes
    public void sourceErrorsrMonitor() {
        scheduleContainer(MONITORING_IMAGE, new ArrayList<String>(), 100l, MonitoringType.SourceErrors.name());
    }

}
