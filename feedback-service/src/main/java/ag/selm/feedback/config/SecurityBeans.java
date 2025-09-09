package ag.selm.feedback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityBeans {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http

                //доступ к ресурсам только авторизированным пользователям
                .authorizeExchange(configurer -> configurer.anyExchange().authenticated())
                //отключаем, тк без состояния
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                //репозиторий контекста безопасности, который занимается хранением информации о пользователе между запросами
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                //сервер ресурсов. В качестве токена jwt
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))

                .build();
    }
}
