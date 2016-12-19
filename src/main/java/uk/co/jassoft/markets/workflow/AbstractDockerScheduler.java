package uk.co.jassoft.markets.workflow;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonshaw on 02/09/2016.
 */
public abstract class AbstractDockerScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDockerScheduler.class);

    public static final String EOD_IMAGE = "marketreaction/eod-data-loader";
    public static final String PREDICTOR_IMAGE = "marketreaction/predictor";
    public static final String SENTIMENT_IMAGE = "marketreaction/sentiment";
    public static final String MONITORING_IMAGE = "marketreaction/monitoring";

    @Value("${APPLICAITON_VERSION:latest}")
    private String version;

    @Value("${ACTIVEMQ_PORT_61616_TCP_ADDR:activemq}")
    private String activeMQHost;

    @Value("${ACTIVEMQ_PORT_61616_TCP_PORT:61616}")
    private int activeMQPort;

    @Value("${MONGO_PORT_27017_TCP_ADDR:mongo}")
    private String mongoDbHost;

    @Value("${MONGO_PORT_27017_TCP_PORT:27017}")
    private int mongoDbPort;

    @Autowired
    private DockerClient docker;

    protected void scheduleContainer(String image, final List<String> envs, Long memLimit, String... cmd) {
        try {

//            Optional<Container> containerOptional = docker.listContainers(DockerClient.ListContainersParam.allContainers())
//                    .stream()
//                    .filter(container -> container.image().contains("workflow"))
//                    .filter(container1 -> container1.image().contains(version))
//                    .findFirst();
//
//            if(!containerOptional.isPresent()) {
//                throw new Exception("No Workflow Container");
//            }

//            Optional<Network> networkOptional = docker.listNetworks().stream()
//                    .filter(network -> network.driver().equals("overlay"))
//                    .filter(network1 -> network1.containers().containsKey(containerOptional.get().id()))
//                    .findFirst();

//            if(!containerOptional.isPresent()) {
//                throw new Exception("No Overlay Network");
//            }
//
            final HostConfig hostConfig = HostConfig.builder()
                    .build();

            List<String> containerEnvs = new ArrayList<>();

            containerEnvs.add("ACTIVEMQ_PORT_61616_TCP_ADDR=" + activeMQHost);
            containerEnvs.add("ACTIVEMQ_PORT_61616_TCP_PORT=" + activeMQPort);
            containerEnvs.add("MONGO_PORT_27017_TCP_ADDR=" + mongoDbHost);
            containerEnvs.add("MONGO_PORT_27017_TCP_PORT=" + mongoDbPort);

            containerEnvs.addAll(envs);

            // Create container with exposed ports
            final ContainerConfig containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(image + ":" + version)
                    .env(containerEnvs)
                    .memory(memLimit * 1024 * 1024)
                    .cmd(cmd)
                    .build();

            final ContainerCreation creation = docker.createContainer(containerConfig);
            final String id = creation.id();

            // Connect to network
//            docker.connectToNetwork(id, networkOptional.get().id());

            int attempts = 5;

            while(attempts > 0) {
                try {
                    // Start container
                    docker.startContainer(id);
                    attempts = 0;
                    break;
                }
                catch (Exception exception) {
                    attempts--;
                    if(attempts > 0) {
                        LOG.info("Failed to start container [{}] Remaining attempts [{}]", image, attempts);
                        Thread.sleep(2000); // Wait 2 seconds before trying again
                        continue;
                    }

                    LOG.error("Failed to start Container [{}]", image);
                }
            }

            docker.waitContainer(id);
        }
        catch (final Exception exception) {
            LOG.error(exception.getLocalizedMessage(), exception);

            throw new RuntimeException(exception);
        }
    }
}
