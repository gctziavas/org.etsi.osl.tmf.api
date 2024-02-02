package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.EventSubscriptionInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

public class EventSubscriptionInputTest {

    @Test
    void testEventSubscriptionInput() {
        EventSubscriptionInput input = new EventSubscriptionInput();
        String callback = "testCallback";
        String query = "testQuery";

        input.callback(callback);
        input.query(query);

        assertEquals(callback, input.getCallback());
        assertEquals(query, input.getQuery());

        input = new EventSubscriptionInput();
        input.setCallback(callback);
        input.setQuery(query);

        assertEquals(callback, input.getCallback());
        assertEquals(query, input.getQuery());
    }

    @Test
    void testEquals() {
        String callback = "testCallback";
        String query = "testQuery";

        EventSubscriptionInput input1 = new EventSubscriptionInput();
        input1.setCallback(callback);
        input1.setQuery(query);

        EventSubscriptionInput input2 = new EventSubscriptionInput();
        input2.setCallback(callback);
        input2.setQuery(query);

        assertTrue(input1.equals(input2));

        input1.callback("differentCallback");

        assertFalse(input1.equals(input2));
    }

    @Test
    void testHashCode() {
        String callback = "testCallback";
        String query = "testQuery";

        EventSubscriptionInput input1 = new EventSubscriptionInput();
        input1.setCallback(callback);
        input1.setQuery(query);

        EventSubscriptionInput input2 = new EventSubscriptionInput();
        input2.setCallback(callback);
        input2.setQuery(query);

        assertEquals(input1.hashCode(), input2.hashCode());

        input1.callback("differentCallback");

        assertNotEquals(input1.hashCode(), input2.hashCode());
    }

    @Test
    void testToString() {
        EventSubscriptionInput input = new EventSubscriptionInput();

        String callback = "testCallback";
        String query = "testQuery";

        input.callback(callback);
        input.query(query);

        String expectedString = "class EventSubscriptionInput {\n" +
                "    callback: testCallback\n" +
                "    query: testQuery\n" +
                "}";

        assertEquals(expectedString, input.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        EventSubscriptionInput input = new EventSubscriptionInput();

        Method method = EventSubscriptionInput.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String inputString = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(input, inputString);

        assertEquals(expectedOutput, output);
    }
}