package uk.co.jassoft.markets.workflow.company;

import uk.co.jassoft.markets.datamodel.exclusion.Exclusion;
import uk.co.jassoft.markets.datamodel.story.NameCount;
import uk.co.jassoft.markets.datamodel.system.Queue;
import uk.co.jassoft.markets.repository.CompanyRepository;
import uk.co.jassoft.markets.repository.ExclusionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by jonshaw on 27/01/2016.
 */
@Component
public class AutoExclusionAdder {

    private static final Logger LOG = LoggerFactory.getLogger(AutoExclusionAdder.class);

    @Autowired
    private ExclusionRepository exclusionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JmsTemplate jmsTemplateTopic;

    void exclusionAdded(final String message) {
        jmsTemplateTopic.convertAndSend(Queue.ExclusionAdded.toString(), message);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void findExclusions() {

        List<NameCount> commonNamedEntities = companyRepository.findCommonNamedEntities(new PageRequest(0, 100));

        commonNamedEntities.stream().filter(isAboveThreshold()).forEach(nameCount -> {

            if(exclusionRepository.findByName(nameCount.getName()).isEmpty()) {
                LOG.info("Adding Auto Exclusion for word [{}]", nameCount.getName());
                exclusionRepository.save(new Exclusion(nameCount.getName(), true));
                exclusionAdded(nameCount.getName());
            }
        });

    }

    public static Predicate<NameCount> isAboveThreshold() {
        return nameCount -> nameCount.getCount() > 40;
    }
}

