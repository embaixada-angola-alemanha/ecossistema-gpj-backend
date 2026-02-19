package ao.gov.embaixada.gpj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GpjApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpjApplication.class, args);
    }
}
