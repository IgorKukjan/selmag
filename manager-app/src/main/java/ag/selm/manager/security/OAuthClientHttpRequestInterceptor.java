package ag.selm.manager.security;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

@RequiredArgsConstructor
public class OAuthClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    //нужно получить токен доступа для текущего пользователя
    //используя токен доступа обратиться к защищенному ресурсу
    private final OAuth2AuthorizedClientManager auth2AuthorizedClientManager;

    //идентификатор регистрации oauth, относительно которой нужно получить ключи доступа
    private final String registrationId;

    //информация о пользователе
    //контекст безопасности
    @Setter
    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            //предоставляет авторизированного клиента
            OAuth2AuthorizedClient authorizedClient = this.auth2AuthorizedClientManager
                    .authorize(OAuth2AuthorizeRequest.withClientRegistrationId(this.registrationId)
                            //необходимо получить пользователя от имени которого будем запрашивать ключ длступа
                            .principal(this.securityContextHolderStrategy.getContext().getAuthentication())
                            .build());
            request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        }

        return execution.execute(request, body);
    }
}
