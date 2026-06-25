package cl.usm.tallerhdd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class TallerHddApplication {

    public static void main(String[] args) {
        SpringApplication.run(TallerHddApplication.class, args);
    }

}
