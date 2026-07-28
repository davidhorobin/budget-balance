package com.davidhorobin.budgetbalance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.NoSuchElementException;

@SpringBootApplication
public class BudgetBalanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetBalanceApplication.class, args);
    }

}
