package ag.selm.feedback.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.testcontainers.containers.MongoDBContainer;

import static org.mockito.Mockito.mock;

@Configuration
public class TestBeans {

    @Bean(initMethod = "start", destroyMethod = "stop")
    //интегрирует контейнер в spring boot и предоставляет данные для подключения к монгодб
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:7");
    }

    //у нас включен resource server.
    // Для того чтоб он корректно работал необходимо указать парметр jwt
    //В тестах не настроены(нет keycloak) -> необходимо замокать ReactiveJwtDecoder
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(){
        return mock(ReactiveJwtDecoder.class);
    }
}
