package org.etsi.osl.tmf.po622.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CLevel;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.common.model.Notification;
import org.etsi.osl.tmf.po622.model.ProductOrderAttributeValueChangeNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderCreateNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderDeleteNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderStateChangeNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Configuration
// @RefreshScope
@Component
public class ProductOrderApiRouteBuilderEvents extends RouteBuilder {

  private static final transient Log logger =
      LogFactory.getLog(ProductOrderApiRouteBuilderEvents.class.getName());



  @Value("${EVENT_PRODUCT_ORDER_CREATE}")
  private String EVENT_PRODUCT_ORDER_CREATE = "";

  @Value("${EVENT_PRODUCT_ORDER_STATE_CHANGED}")
  private String EVENT_PRODUCT_ORDER_STATE_CHANGED = "";

  @Value("${EVENT_PRODUCT_ORDER_DELETE}")
  private String EVENT_PRODUCT_ORDER_DELETE = "";

  @Value("${EVENT_PRODUCT_ORDER_ATTRIBUTE_VALUE_CHANGED}")
  private String EVENT_PRODUCT_ORDER_ATTRIBUTE_VALUE_CHANGED = "";



  @Value("${spring.application.name}")
  private String compname;

  @Autowired
  private ProducerTemplate template;


  @Autowired
  private CentralLogger centralLogger;

  @Override
  public void configure() throws Exception {



  }

  /**
   * @param n
   */
  @Transactional
  public void publishEvent(final Notification n, final String objId) {
    n.setEventType(n.getClass().getName());
    logger.info("will send Event for type " + n.getEventType());
    try {
      String msgtopic = "";

      if (n instanceof ProductOrderCreateNotification) {
        msgtopic = EVENT_PRODUCT_ORDER_CREATE;
      } else if (n instanceof ProductOrderStateChangeNotification) {
        msgtopic = EVENT_PRODUCT_ORDER_STATE_CHANGED;
      } else if (n instanceof ProductOrderDeleteNotification) {
        msgtopic = EVENT_PRODUCT_ORDER_DELETE;
      } else if (n instanceof ProductOrderAttributeValueChangeNotification) {
        msgtopic = EVENT_PRODUCT_ORDER_ATTRIBUTE_VALUE_CHANGED;
      }
      Map<String, Object> map = new HashMap<>();
      map.put("eventid", n.getEventId());
      map.put("objId", objId);

      String apayload = toJsonString(n);
      template.sendBodyAndHeaders(msgtopic, apayload, map);


      centralLogger.log(CLevel.INFO, apayload, compname);

    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Cannot send Event . " + e.getMessage());
    }
  }


  static String toJsonString(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsString(object);
  }

}
