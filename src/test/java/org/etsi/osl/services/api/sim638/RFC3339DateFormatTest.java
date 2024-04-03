package org.etsi.osl.services.api.sim638;

import java.text.FieldPosition;
import java.util.Date;

import org.etsi.osl.tmf.sim638.api.RFC3339DateFormat;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class RFC3339DateFormatTest {

    @Test
    public void testFormat() {
        RFC3339DateFormat rfc3339DateFormat = new RFC3339DateFormat();
        Date date = new Date(1637484321000L);

        StringBuffer stringBuffer = new StringBuffer();
        FieldPosition fieldPosition = new FieldPosition(0);
        StringBuffer formattedDate = rfc3339DateFormat.format(date, stringBuffer, fieldPosition);

        assertEquals("2021-11-21T08:45:21.000Z", formattedDate.toString());
    }
}
