package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDTO;
import com.ead.authuser.dtos.ResponsePageDTO;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe que faz chamadas à API do microsserviço course
 */
@Log4j2
@Component
public class CourseClient {

    private final RestTemplate restTemplate;
    private final UtilsService utilsService;

    @Value("${ead.api.url.course}")
    private String REQUEST_URL_COURSE;

    @Autowired
    public CourseClient(final RestTemplate restTemplate, final UtilsService utilsService) {
        this.restTemplate = restTemplate;
        this.utilsService = utilsService;
    }

    //    @Retry(name = "retryInstance", fallbackMethod = "retryfallback")
    @CircuitBreaker(name = "circuitbreakerInstance")
    public Page<CourseDTO> getAllCoursesByUser(final UUID userId,
                                               final Pageable pageable,
                                               final String token) {
        final List<CourseDTO> searchResult;

        ResponseEntity<ResponsePageDTO<CourseDTO>> result = null;

        final String url = this.REQUEST_URL_COURSE + this.utilsService.createUrl(userId, pageable);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        final HttpEntity<String> requestEntity = new HttpEntity<>("parameters", headers);

        log.debug("Request URL: {} ", url);

        log.info("Request URL: {} ", url);


        final ParameterizedTypeReference<ResponsePageDTO<CourseDTO>> responseType
                = new ParameterizedTypeReference<>() {
        };

        result = this.restTemplate
                .exchange(url, HttpMethod.GET, requestEntity, responseType);

        searchResult = result.getBody().getContent();

        log.debug("Response Number of Elements: {} ", searchResult.size());
        log.info("Ending request /courses userId {} ", userId);
        return result.getBody();
    }

    /**
     * Método de fallback utilizado quando o número de tentativas de requisição é excedido
     * e o circuit breaker entra no estado OPEN.
     * Foi criado apenas como forma de ilustrar o processo, não possuindo significado na
     * lógica do negócio, já que o cliente deve ser que o ms de course está indisponível.
     * <br>
     * Observação: os métodos de fallback devem ter os mesmos parâmetros do método princi-
     * pal.
     * <br>
     *
     * @param userId
     * @param pageable
     * @param t
     * @return
     */
    public Page<CourseDTO> circuitbreakerfallback(final UUID userId,
                                                  final Pageable pageable,
                                                  final Throwable t) {
        log.error("Inside circuit breaker fallback, cause - {}", t.toString());
        final List<CourseDTO> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    public Page<CourseDTO> retryfallback(final UUID userId, final Pageable pageable, final Throwable t) {
        log.error("Inside retry retryfallback, cause - {}", t.toString());
        final List<CourseDTO> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

}
