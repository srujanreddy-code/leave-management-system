package com.ust.lms;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main entry point for the Leave Management System application.
 * Configures component scanning, scheduling, password encoding,
 * and ModelMapper.
 */
@SpringBootApplication
@ComponentScan({"com.ust.lms.web.controller", "com.ust.lms"})
@EnableScheduling
public class LeaveManagementSystemApplication {

    private final Environment environment;

    public LeaveManagementSystemApplication(Environment environment) {
        this.environment = environment;
    }

    /**
     * Starts the Leave Management System application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LeaveManagementSystemApplication.class, args);
    }

    /**
     * Creates the password encoder used for securely hashing passwords.
     *
     * @return configured BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates and configures the ModelMapper used for mapping
     * between entities and DTOs.
     *
     * @return configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.getConfiguration().setSkipNullEnabled(true);
        mapper.getConfiguration().setCollectionsMergeEnabled(false);
        return mapper;
    }

}