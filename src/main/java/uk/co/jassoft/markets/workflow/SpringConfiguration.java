package uk.co.jassoft.markets.workflow;

import uk.co.jassoft.markets.BaseSpringConfiguration;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by jonshaw on 13/07/15.
 */
@Configuration
@ComponentScan("uk.co.jassoft.markets.workflow")
@EnableScheduling
public class SpringConfiguration extends BaseSpringConfiguration {

    @Value("${SWARM_CLUSTER_URL}")
    private String swarmClusterUrl;

    @Bean
    public DockerClient dockerClient() throws DockerCertificateException {
        // Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
        return DefaultDockerClient.builder()
                .uri(swarmClusterUrl)
                .readTimeoutMillis(DefaultDockerClient.NO_TIMEOUT) // To account for pulling image on 1st run
                .build();
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringConfiguration.class, args);
    }


}
