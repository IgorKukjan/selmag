package ag.selm.manager.config;

import ag.selm.manager.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }



    @Bean
    @Primary//во время тестов необходимо чтобы данный компонент использовался а не который в ClientsBeans
    public RestClientProductsRestClient testRestClientProductsRestClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:54321}") String catalogueBaseUri
    ) {
        var client = (HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build());
        var requestFactory = new JdkClientHttpRequestFactory(client);
        return new RestClientProductsRestClient(RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(catalogueBaseUri)
                .build());
    }
}
