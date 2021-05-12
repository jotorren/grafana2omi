package cat.catsalut.hes.monitoring.service;

import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import cat.catsalut.hes.monitoring.configuration.MonitoringConfigurationProperties;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FreemarkerService {

	private final Configuration freemarkerCfg;
	private final MonitoringConfigurationProperties conf;
	
	private Template template;
	
	@PostConstruct
	public void init() throws IOException {
		String fileName = conf.getOmiTemplate();
		String templateFtl = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);
		log.info("Parsing template: {}", fileName);

		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("omi", templateFtl);
		freemarkerCfg.setTemplateLoader(stringLoader);
		template = freemarkerCfg.getTemplate("omi");
		log.info("Loading template: {}", "omi");
	}

	public String parseTemplate(Object data) throws IOException, TemplateException {
		return processTemplateIntoString(template, data);
	}
}
