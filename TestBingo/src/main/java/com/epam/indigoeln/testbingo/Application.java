package com.epam.indigoeln.testbingo;

import com.epam.indigoeln.testbingo.repository.BingoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Autowired
    private BingoRepository bingoRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        LOGGER.info("------------------------------------------------------------------------------");

        try {
            bingoRepository.testMoleculeRepository();
        } catch (Exception e) {
            LOGGER.error("Error testing molecule repository: " + e);
        }

        LOGGER.info("------------------------------------------------------------------------------");

        try {
            bingoRepository.testReactionRepository();
        } catch (Exception e) {
            LOGGER.error("Error testing reaction repository: " + e);
        }

        LOGGER.info("------------------------------------------------------------------------------");
    }
}
