package com.association.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.association")
public class AppConfig {
    // Ce fichier dit à Spring où chercher les @Service, @Repository, etc.
}
