package cat.catsalut.hes.monitoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "monitoring")
@Getter @Setter
public class MonitoringConfigurationProperties {

    String omiUrl;
    String omiTemplate;
}
