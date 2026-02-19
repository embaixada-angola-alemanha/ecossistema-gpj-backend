package ao.gov.embaixada.gpj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient healthCheckRestClient(
            @Value("${gop.health-poller.timeout-ms:5000}") int timeoutMs) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient prometheusRestClient(
            @Value("${gop.prometheus.url:http://localhost:9090}") String prometheusUrl) {
        return RestClient.builder()
                .baseUrl(prometheusUrl)
                .build();
    }
}
