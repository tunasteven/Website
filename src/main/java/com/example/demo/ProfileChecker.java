package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProfileChecker implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) {
        String[] activeProfiles = env.getActiveProfiles();
        System.out.println("🌟 Active Profiles: " + String.join(", ", activeProfiles));
    }
}
