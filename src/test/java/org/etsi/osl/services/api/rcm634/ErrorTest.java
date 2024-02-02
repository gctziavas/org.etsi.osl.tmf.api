package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.Error;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

public class ErrorTest {

    @Test
    void testError() {
        Error error = new Error();
        String code = "testCode";
        String reason = "testReason";
        String message = "testMessage";
        String status = "testStatus";
        String referenceError = "testReferenceError";

        error.code(code);
        error.reason(reason);
        error.message(message);
        error.status(status);
        error.referenceError(referenceError);

        assertEquals(code, error.getCode());
        assertEquals(reason, error.getReason());
        assertEquals(message, error.getMessage());
        assertEquals(status, error.getStatus());
        assertEquals(referenceError, error.getReferenceError());

        error = new Error();
        error.setCode(code);
        error.setReason(reason);
        error.setMessage(message);
        error.setStatus(status);
        error.setReferenceError(referenceError);

        assertEquals(code, error.getCode());
        assertEquals(reason, error.getReason());
        assertEquals(message, error.getMessage());
        assertEquals(status, error.getStatus());
        assertEquals(referenceError, error.getReferenceError());
    }

    @Test
    void testEquals() {
        String code = "testCode";
        String reason = "testReason";
        String message = "testMessage";
        String status = "testStatus";
        String referenceError = "testReferenceError";

        Error error1 = new Error();
        error1.setReason(reason);
        error1.setMessage(message);
        error1.setStatus(status);
        error1.setReferenceError(referenceError);

        Error error2 = new Error();
        error2.setReason(reason);
        error2.setMessage(message);
        error2.setStatus(status);
        error2.setReferenceError(referenceError);

        assertTrue(error1.equals(error2));

        error1.code("differentCode");

        assertFalse(error1.equals(error2));
    }

    @Test
    void testHashCode() {
        String code = "testCode";
        String reason = "testReason";
        String message = "testMessage";
        String status = "testStatus";
        String referenceError = "testReferenceError";

        Error error1 = new Error();
        error1.setReason(reason);
        error1.setMessage(message);
        error1.setStatus(status);
        error1.setReferenceError(referenceError);

        Error error2 = new Error();
        error2.setReason(reason);
        error2.setMessage(message);
        error2.setStatus(status);
        error2.setReferenceError(referenceError);

        assertEquals(error1.hashCode(), error2.hashCode());

        error1.code("differentCode");

        assertNotEquals(error1.hashCode(), error2.hashCode());
    }

    @Test
    void testToString() {
        Error error = new Error();

        String code = "testCode";
        String reason = "testReason";
        String message = "testMessage";
        String status = "testStatus";
        String referenceError = "testReferenceError";
        String baseType = "testBaseType";
        String schemaLocation = "testSchemaLocation";
        String type = "testType";

        error.code(code);
        error.reason(reason);
        error.message(message);
        error.status(status);
        error.referenceError(referenceError);

        String expectedString = "class Error {\n" +
                "    code: testCode\n" +
                "    reason: testReason\n" +
                "    message: testMessage\n" +
                "    status: testStatus\n" +
                "    referenceError: testReferenceError\n" +
                "    _atBaseType: null\n" +
                "    _atSchemaLocation: null\n" +
                "    _atType: null\n" +
                "}";

        assertEquals(expectedString, error.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        Error error = new Error();

        Method method = Error.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(error, input);

        assertEquals(expectedOutput, output);
    }

    @Test
    void testAtBaseType() {
        Error error = new Error();
        String baseType = "testBaseType";
        error.setAtBaseType(baseType);
        assertEquals(baseType, error.getAtBaseType());

        Error newError = error._atBaseType("newTestBaseType");
        assertEquals(error, newError);
    }

    @Test
    void testAtSchemaLocation() {
        Error error = new Error();
        String schemaLocation = "testSchemaLocation";
        error.setAtSchemaLocation(schemaLocation);
        assertEquals(schemaLocation, error.getAtSchemaLocation());

        Error newError = error._atSchemaLocation("newTestSchemaLocation");
        assertEquals(error, newError);
    }

    @Test
    void testAtType() {
        Error error = new Error();
        String type = "testType";
        error.setAtType(type);
        assertEquals(type, error.getAtType());

        Error newError = error._atType("newTestType");
        assertEquals(error, newError);
    }
}