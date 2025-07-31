package ag.selm.catalogue;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class CatalogueServiceApplicationTest {
    @Test
    public void testMainWithMock() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            springApplicationMock.when(() -> SpringApplication.run(CatalogueServiceApplication.class, new String[] {}))
                    .thenReturn(mockContext);

            CatalogueServiceApplication.main(new String[] {});

            springApplicationMock.verify(() -> SpringApplication.run(CatalogueServiceApplication.class, new String[] {}));
        }
    }
}