package com.mmadu.identity;

import com.mmadu.identity.config.DomainIdentityConfigurationList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(DomainIdentityConfigurationList.class)
public class MmaduIdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(MmaduIdentityApplication.class, args);
    }

}
