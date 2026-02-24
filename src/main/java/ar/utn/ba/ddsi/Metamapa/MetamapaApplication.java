package ar.utn.ba.ddsi.Metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MetamapaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetamapaApplication.class, args);
	}

}
