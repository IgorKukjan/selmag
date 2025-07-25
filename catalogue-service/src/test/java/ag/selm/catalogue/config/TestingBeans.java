package ag.selm.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    //тк у нас есть oauth2ResourceServer
    //Нам нужно его конфигурацию замокать компонент на время тестирование
    //тк у нас нет реально сконфигурированного ресурс сервера
    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}