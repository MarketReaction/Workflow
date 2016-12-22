package uk.co.jassoft.markets.workflow.dates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.co.jassoft.markets.datamodel.story.date.MissingDateFormat;
import uk.co.jassoft.markets.repository.MissingDateFormatRepository;
import uk.co.jassoft.markets.utils.article.ContentGrabber;

import java.util.Date;
import java.util.List;

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
        int pageCount = 0;

        while (true) {
            Page<MissingDateFormat> missingDateFormatsPage = missingDateFormatRepository.findAll(new PageRequest(pageCount, 100));

            List<MissingDateFormat> missingDateFormats = missingDateFormatsPage.getContent();

            if(missingDateFormats.isEmpty()) {
                break;
            }

            missingDateFormats.parallelStream().forEach(missingDateFormat -> {
                Date publishedDate = contentGrabber.getPublishedDate(missingDateFormat.getMetatag());

                if(publishedDate != null) {
                    missingDateFormatRepository.delete(missingDateFormat.getId());
                }
            });


            pageCount++;
        }
    }
}
