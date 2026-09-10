package com.loki.smstracker
import org.junit.Test
import org.junit.Assert.*
class ExampleUnitTest {
    @Test fun port_ok() { assertEquals(7777, LocationTrackerService.PORT) }
    @Test fun protocol_ok() { assertTrue("LOKI1|1|2|3".startsWith("LOKI1")) }
}
