package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.etsi.osl.tmf.rcm634.model.EventSubscription;
import org.junit.jupiter.api.Test;

public class EventSubscriptionTest {

    @Test
    void testEventSubscription() {
        EventSubscription subscription = new EventSubscription();
        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";

        subscription.id(id);
        subscription.callback(callback);
        subscription.query(query);

        assertEquals(id, subscription.getId());
        assertEquals(callback, subscription.getCallback());
        assertEquals(query, subscription.getQuery());

        subscription = new EventSubscription();
        subscription.setId(id);
        subscription.setCallback(callback);
        subscription.setQuery(query);

        assertEquals(id, subscription.getId());
        assertEquals(callback, subscription.getCallback());
        assertEquals(query, subscription.getQuery());
    }

    @Test
    void testEquals() {
        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";

        EventSubscription subscription1 = new EventSubscription();
        subscription1.setId(id);
        subscription1.setCallback(callback);
        subscription1.setQuery(query);

        EventSubscription subscription2 = new EventSubscription();
        subscription2.setId(id);
        subscription2.setCallback(callback);
        subscription2.setQuery(query);

        assertTrue(subscription1.equals(subscription2));

        subscription1.id("differentId");

        assertFalse(subscription1.equals(subscription2));
    }

    @Test
    void testHashCode() {
        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";

        EventSubscription subscription1 = new EventSubscription();
        subscription1.setId(id);
        subscription1.setCallback(callback);
        subscription1.setQuery(query);

        EventSubscription subscription2 = new EventSubscription();
        subscription2.setId(id);
        subscription2.setCallback(callback);
        subscription2.setQuery(query);

        assertEquals(subscription1.hashCode(), subscription2.hashCode());

        subscription1.id("differentId");

        assertNotEquals(subscription1.hashCode(), subscription2.hashCode());
    }

    @Test
    void testToString() {
        EventSubscription subscription = new EventSubscription();

        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";

        subscription.id(id);
        subscription.callback(callback);
        subscription.query(query);

        String expectedString = "class EventSubscription {\n" +
                "    id: testId\n" +
                "    callback: testCallback\n" +
                "    query: testQuery\n" +
                "}";

        assertEquals(expectedString, subscription.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        EventSubscription subscription = new EventSubscription();

        Method method = EventSubscription.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(subscription, input);

        assertEquals(expectedOutput, output);
    }
}