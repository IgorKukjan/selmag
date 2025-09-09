package ag.selm.customer.config;

import ag.selm.customer.client.WebClientFavouriteProductsClient;
import ag.selm.customer.client.WebClientProductReviewsClient;
import ag.selm.customer.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    @Scope("prototype")
    public WebClient.Builder selmagServicesWebClientBuilder(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository
    ) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                        authorizedClientRepository);
        filter.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                //перехватывать исходящие запросы и добовлять дополнительные заголовки для авторизации
                //и перенеправлять пользователя на страницу авторизации
                .filter(filter);
    }

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientProductsClient(selmagServicesWebClientBuilder
                                            .baseUrl(catalogueBaseUri)
                                            .build());
    }

    @Bean
    public WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${selmag.services.feedback.uri:http://localhost:8084}") String feedbackBaseUri,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientFavouriteProductsClient(selmagServicesWebClientBuilder
                .baseUrl(feedbackBaseUri)
                .build());
    }

    @Bean
    public WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${selmag.services.feedback.uri:http://localhost:8084}") String feedbackBaseUri,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientProductReviewsClient(selmagServicesWebClientBuilder
                .baseUrl(feedbackBaseUri)
                .build());
    }
}
