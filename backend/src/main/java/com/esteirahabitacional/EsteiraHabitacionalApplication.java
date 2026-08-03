package com.esteirahabitacional;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulith
@EnableScheduling
public class EsteiraHabitacionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsteiraHabitacionalApplication.class, args);
    }
}
