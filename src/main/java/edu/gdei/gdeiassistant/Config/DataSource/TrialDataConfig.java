package edu.gdei.gdeiassistant.Config.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:/config/trial/data.properties", encoding = "UTF-8")
public class TrialDataConfig {

}
