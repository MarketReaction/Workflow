/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.jassoft.markets.workflow.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.jassoft.markets.datamodel.sources.Source;
import uk.co.jassoft.markets.datamodel.sources.SourceType;
import uk.co.jassoft.markets.datamodel.sources.SourceUrl;
import uk.co.jassoft.markets.datamodel.story.Story;
import uk.co.jassoft.markets.datamodel.system.Queue;
import uk.co.jassoft.markets.repository.SourceRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;
import java.util.function.Predicate;

/**
 *
 * @author Jonny
 */
@Component
public class CrawlerStarter {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerStarter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private JmsTemplate jmsTemplate;
    
    void crawlLink(final Object message) throws JsonProcessingException
    {
        jmsTemplate.convertAndSend(Queue.Crawler.toString(), mapper.writeValueAsString(message));
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void start()
    {
        LOG.info("Triggered CrawlerStarter");

        sourceRepository.findByTypeAndDisabled(SourceType.Crawler, false)
                .parallelStream()
                .forEach(source -> {
                    source.getUrls()
                            .parallelStream()
                            .filter(sourceUrl -> sourceUrl.isEnabled())
                            .filter(notDisabled())
                            .filter(notPendingCrawl())
                            .filter(exceedsCrawlInterval())
                            .forEach(sourceUrl -> {
                                try {
                                    sourceUrl.setPendingCrawl(true);

                                    mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(source.getId()).and("urls.url").is(sourceUrl.getUrl().toString())),
                                            new Update().set("urls.$", sourceUrl)
                                            , Source.class);

                                    LOG.debug("Crawling URL [{}] from Source [{}]", sourceUrl.getUrl(), source.getName());

                                    crawlLink(new Story(source.getName(), new URL(sourceUrl.getUrl()), new Date(), source.getId()));

                                } catch (Exception exception) {
                                    LOG.error(exception.getLocalizedMessage(), exception);
                                }
                            });
                    sourceRepository.save(source);
                });
    }

    public static Predicate<SourceUrl> notDisabled() {
        return sourceUrl -> sourceUrl.getDisabledUntil() == null ? true : new DateTime(sourceUrl.getDisabledUntil()).isBeforeNow();
    }

    public static Predicate<SourceUrl> exceedsCrawlInterval() {
        return sourceUrl -> (sourceUrl.getLastCrawled() == null || sourceUrl.getCrawlInterval() == null) ? true
                : new DateTime(sourceUrl.getLastCrawled()).plusSeconds(sourceUrl.getCrawlInterval()).isBeforeNow();
    }

    public static Predicate<SourceUrl> notPendingCrawl() {
        return sourceUrl -> !sourceUrl.isPendingCrawl();
    }
}
