package cat.catsalut.hes.monitoring;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;

@SpringBootApplication(exclude = JmxAutoConfiguration.class)
public class MonitoringApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MonitoringApplication.class, args);
	}
}
