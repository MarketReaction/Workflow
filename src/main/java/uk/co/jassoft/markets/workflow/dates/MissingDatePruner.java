package uk.co.jassoft.markets.workflow.dates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.co.jassoft.markets.datamodel.story.date.MissingDateFormat;
import uk.co.jassoft.markets.repository.MissingDateFormatRepository;
import uk.co.jassoft.markets.utils.article.ContentGrabber;

import java.util.Date;

/**
 * Created by jonshaw on 22/01/15.
 */
@Component
@Import({ContentGrabber.class})
public class MissingDatePruner {

    @Autowired
    private MissingDateFormatRepository missingDateFormatRepository;

    @Autowired
    private ContentGrabber contentGrabber;

    @Scheduled(cron = "0 0 * * * ?")
    public void prune()
    {
        // TODO this needs to be paged
        for(MissingDateFormat missingDateFormat : missingDateFormatRepository.findAll(new PageRequest(0, 100))) {
            Date publishedDate = contentGrabber.getPublishedDate(missingDateFormat.getMetatag());

            if(publishedDate != null) {
                missingDateFormatRepository.delete(missingDateFormat.getId());
            }

        }
    }
}
