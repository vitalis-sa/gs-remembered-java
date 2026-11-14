package fiap.remembered;

import fiap.remembered.util.Fuso;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class RememberedApplication {

	/** Todo horario da API (JSON, banco, jobs) usa o mesmo fuso, qualquer que seja o do servidor. */
	static {
		TimeZone.setDefault(TimeZone.getTimeZone(Fuso.ZONA));
	}

	public static void main(String[] args) {
		SpringApplication.run(RememberedApplication.class, args);
	}
}
