package kr.co.hanipcloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

@ConfigurationPropertiesScan
@EnableWebFlux
@SpringBootApplication
public class HanipCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanipCloudGatewayApplication.class, args);
    }

}
