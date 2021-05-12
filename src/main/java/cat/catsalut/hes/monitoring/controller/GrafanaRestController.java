package cat.catsalut.hes.monitoring.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cat.catsalut.hes.monitoring.configuration.MonitoringConfigurationProperties;
import cat.catsalut.hes.monitoring.service.FreemarkerService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GrafanaRestController {

	private static final Map<String, String> GRAFANA_TO_OMI = Map.of(
			"title", "title", 
			"message", "description"
	);

	private static final Map<String, String> GRAFANA_TAGS_TO_OMI = Map.of(
			"Criticitat", "severity",
			"Namespace", "node",
			"Tipus", "object",
			"Categoria", "category",
			"Origen", "affectedCI"
	);
	
	private static final String STATE_FIELD = "state";
	private static final String TAGS_FIELD = "tags";
	
	private static final String GRAFANA_STATE_ALERTING = "alerting";
	private static final String OMI_SEVERITY_KEY = "severity";	
	private static final String OMI_SEVERITY_RESTORED = "NORMAL";
	private static final String OMI_SUBCATEGORY_KEY = "subcategory";
	private static final String OMI_SUBCATEGORY_VALUE = "Grafana";
	
	private final FreemarkerService service;
	private final RestTemplate restTemplate;
	private final MonitoringConfigurationProperties conf;
	
	@PostMapping(value = "/omi/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> grafanaWebHook(@RequestBody String alert, HttpServletResponse response) {
		log.info(alert);

		Map<String, String> data = new HashMap<String, String>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode json = mapper.readTree(alert);
			GRAFANA_TO_OMI.entrySet().forEach(entry -> {
				String value = json.get(entry.getKey()).asText();
				data.put(entry.getValue(), value);
			});

			JsonNode tags = json.get(TAGS_FIELD);
			GRAFANA_TAGS_TO_OMI.entrySet().forEach(entry -> {
				String entryKey = tags.get(entry.getKey());
				if (null != entryKey){
					String value = entryKey.asText();
					data.put(entry.getValue(), value);					
				}
			});

			String state = json.get(STATE_FIELD).asText();
			if (!GRAFANA_STATE_ALERTING.equals(state)) {
				data.put(OMI_SEVERITY_KEY, OMI_SEVERITY_RESTORED);
			}

			data.put(OMI_SUBCATEGORY_KEY, OMI_SUBCATEGORY_VALUE);
			
			String omi = service.parseTemplate(data);
			log.info(omi);

			MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<String, String>();
			requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<?> httpEntity = new HttpEntity<Object>(omi, requestHeaders);
			
			ResponseEntity<Void> omiResponse = restTemplate.exchange(conf.getOmiUrl(), HttpMethod.POST, httpEntity, Void.class);
			log.info("OMI Response status: " + omiResponse.getStatusCodeValue());
			
			return ResponseEntity.ok(omi);
		} catch (IOException | TemplateException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

	}
}
