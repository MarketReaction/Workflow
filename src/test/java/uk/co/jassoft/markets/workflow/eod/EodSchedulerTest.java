package uk.co.jassoft.markets.workflow.eod;

import uk.co.jassoft.markets.workflow.SpringConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by jonshaw on 01/09/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@IntegrationTest(value = "EOD_DATA_WEB_SERVICE=http://eod:8080/test.asmx")
@Ignore("Depends on docker")
public class EodSchedulerTest {

    @InjectMocks
    @Autowired
    private EodScheduler target;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testFindCompanies() throws Exception {

        target.findCompanies();

    }
}