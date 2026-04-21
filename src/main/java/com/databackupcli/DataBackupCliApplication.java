package com.databackupcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DataBackupCliApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataBackupCliApplication.class, args);
    }
}
