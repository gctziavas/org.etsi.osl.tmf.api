package org.etsi.osl.services.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.metrics.api.ServiceOrderMetricsApiController;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.etsi.osl.tmf.so641.model.ServiceOrderCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.etsi.osl.tmf.so641.model.ServiceOrderUpdate;
import org.etsi.osl.tmf.so641.reposervices.ServiceOrderRepoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.jayway.jsonpath.JsonPath;


public class ServiceOrderMetricsApiControllerTest  extends BaseIT {

    private MockMvc mvc;

    @Autowired
    ServiceOrderRepoService serviceOrderRepoService;

    @Autowired
    private WebApplicationContext context;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeAll
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    public void tearDown() {
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCountTotalServiceOrders() throws Exception {
        createServiceOrder(ServiceOrderStateType.INPROGRESS);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalServiceOrders" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalServiceOrders = JsonPath.read(response, "$.totalServiceOrders");


        assertThat(totalServiceOrders).isEqualTo(serviceOrderRepoService.findAll().size());
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCountTotalServiceOrdersWithState() throws Exception {
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalServiceOrders" )
                        .param("state", "ACKNOWLEDGED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalServiceOrders = JsonPath.read(response, "$.totalServiceOrders");


        List<ServiceOrder> serviceOrdersList = serviceOrderRepoService.findAll();
        int activeServiceOrders = (int) serviceOrdersList.stream().filter(serviceOrder -> serviceOrder.getState() == ServiceOrderStateType.ACKNOWLEDGED).count();

        assertThat(totalServiceOrders).isEqualTo(activeServiceOrders);
        assertThat(activeServiceOrders).isEqualTo(1);
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetTotalActiveServiceOrders() throws Exception {
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.COMPLETED);
        createServiceOrder(ServiceOrderStateType.COMPLETED);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);
        createServiceOrder(ServiceOrderStateType.REJECTED);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/activeServiceOrders" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalServiceOrders = JsonPath.read(response, "$.activeServiceOrders");

        assertThat(totalServiceOrders).isEqualTo(12);
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
    @Test
    public void testGetServiceOrdersGroupedByDay() throws Exception {
        String startTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);
        createServiceOrder(ServiceOrderStateType.PARTIAL);

        String endTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(4).format(DateTimeFormatter.ISO_INSTANT);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/serviceOrdersGroupByDay")
                        .param("starttime", startTime)
                        .param("endtime", endTime)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> groupByDay = JsonPath.read(response, "$.serviceOrders.aggregations.groupByDay");


        OffsetDateTime start = OffsetDateTime.parse(startTime);
        OffsetDateTime end = OffsetDateTime.parse(endTime);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        Map<String, Integer> dayCounts = groupByDay.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.get("key"),
                        entry -> (Integer) entry.get("count")
                ));

        int totalDays = (int) ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;

        for (int i = 0; i < totalDays; i++) {
            OffsetDateTime day = start.plusDays(i).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
            String dayKey = formatter.format(day.toInstant());

            if (i == 0) {
                // Today: should have all 6
                assertThat(dayCounts.get(dayKey)).isEqualTo(6);
            } else {
                // Other days: should be 0
                assertThat(dayCounts.get(dayKey)).isEqualTo(0);
            }
        }
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
    @Test
    public void testGetServiceOrdersGroupedByState() throws Exception {
        String startTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.INPROGRESS);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);
        createServiceOrder(ServiceOrderStateType.ACKNOWLEDGED);
        createServiceOrder(ServiceOrderStateType.PARTIAL);

        String endTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(4).format(DateTimeFormatter.ISO_INSTANT);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/serviceOrdersGroupByState")
                        .param("starttime", startTime)
                        .param("endtime", endTime)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> groupByState = JsonPath.read(response, "$.serviceOrders.aggregations.groupByState");

        // Create a map from key -> count
        Map<String, Integer> stateCounts = groupByState.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.get("key"),
                        entry -> (Integer) entry.get("count")
                ));

        assertThat(stateCounts.get("INPROGRESS")).isEqualTo(3);
        assertThat(stateCounts.get("ACKNOWLEDGED")).isEqualTo(2);
        assertThat(stateCounts.get("PARTIAL")).isEqualTo(1);
        assertThat(stateCounts.get("INITIAL")).isEqualTo(0);
        assertThat(stateCounts.get("REJECTED")).isEqualTo(0);
        assertThat(stateCounts.get("PENDING")).isEqualTo(0);
        assertThat(stateCounts.get("HELD")).isEqualTo(0);
        assertThat(stateCounts.get("CANCELLED")).isEqualTo(0);
        assertThat(stateCounts.get("COMPLETED")).isEqualTo(0);
        assertThat(stateCounts.get("FAILED")).isEqualTo(0);
    }

    private void createServiceOrder(ServiceOrderStateType stateType) throws Exception {

        ServiceOrderCreate serviceOrder = new ServiceOrderCreate();
        serviceOrder.setCategory("Test Category");
        serviceOrder.setDescription("A Test Service Order");
        serviceOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        serviceOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).plusDays(3).toString());

        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceOrdering/v4/serviceOrder")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceOrder)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ServiceOrder responseSO = JsonUtils.toJsonObj(response, ServiceOrder.class);

        // Update Service Order State
        String soId = responseSO.getId();

        ServiceOrderUpdate servOrderUpd = new ServiceOrderUpdate();
        servOrderUpd.setState(stateType);

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceOrdering/v4/serviceOrder/" + soId)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( servOrderUpd ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ServiceOrder responsesServiceOrder2 = JsonUtils.toJsonObj(response2,  ServiceOrder.class);
        assertThat(responsesServiceOrder2.getState().toString()).isEqualTo(stateType.toString());
    }

}
