
package org.etsi.osl.services.api;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.MOCK,
  classes = OpenAPISpringBoot.class
)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS )
@ExtendWith(SpringExtension.class)   // <-- JUnit 5
@TestInstance(Lifecycle.PER_CLASS)   // <-- allows non-static @AfterAll
@TestMethodOrder(MethodOrderer.DisplayName.class)
@Transactional
public abstract class BaseIT {

    @Autowired
    private DataSource dataSource;

    @AfterAll   
    public void cleanupDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Disable foreign key checks
            statement.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // Drop all user tables
            statement.execute("DROP ALL OBJECTS");

            // Re-enable foreign key checks
            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");

            // Force garbage collection
            System.gc();
            System.out.println("==============================================================================================");;
            System.out.println("==============================================================================================");;
            System.out.println("=========================================="+  this.getClass().getCanonicalName() +"===============================================");;
            System.out.println("==============================================================================================");;
            System.out.println("==============================================================================================");;
            System.out.println("==============================================================================================");;
            Thread.sleep(5000);
        }
    }
}