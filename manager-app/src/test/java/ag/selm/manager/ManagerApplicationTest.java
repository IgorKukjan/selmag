package ag.selm.manager;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ManagerApplicationTest {
    @Test
    public void testMainWithMock() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
            //given
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            springApplicationMock.when(() -> SpringApplication.run(ManagerApplication.class, new String[] {}))
                    .thenReturn(mockContext);

            //when
            ManagerApplication.main(new String[] {});

            //then
            springApplicationMock.verify(() -> SpringApplication.run(ManagerApplication.class, new String[] {}));
        }
    }
}