package cn.gdeiassistant.core.userprofile.controller.request;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePatchRequestTest {

    private final JsonMapper mapper = new JsonMapper();

    @Test
    void absentFieldsAreNotIncluded() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"nickname\":\"new\"}", ProfilePatchRequest.class);

        assertEquals(Map.of("nickname", "new"), request.toPatchMap());
    }

    @Test
    void explicitNullIsIncludedForClearing() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"birthday\":null,\"introduction\":null}", ProfilePatchRequest.class);

        Map<String, Object> patch = request.toPatchMap();
        assertTrue(patch.containsKey("birthday"));
        assertNull(patch.get("birthday"));
        assertTrue(patch.containsKey("introduction"));
        assertNull(patch.get("introduction"));
    }

    @Test
    void nestedObjectsKeepTheirWireShape() throws Exception {
        ProfilePatchRequest request = mapper.readValue(
                "{\"birthday\":{\"year\":2000,\"month\":1,\"date\":2},"
                        + "\"location\":{\"region\":\"CN\",\"state\":\"44\",\"city\":\"1\"}}",
                ProfilePatchRequest.class);

        Map<String, Object> patch = request.toPatchMap();
        assertEquals(Map.of("year", 2000, "month", 1, "date", 2), patch.get("birthday"));
        assertEquals(Map.of("region", "CN", "state", "44", "city", "1"), patch.get("location"));
    }

    @Test
    void unknownFieldsReachTheServiceForRejection() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"username\":\"bob\"}", ProfilePatchRequest.class);

        assertEquals(Map.of("username", "bob"), request.toPatchMap());
    }

    @Test
    void everySupportedFieldRoundTrips() throws Exception {
        ProfilePatchRequest request = mapper.readValue(
                "{\"nickname\":\"n\",\"introduction\":\"i\",\"enrollment\":2024,\"faculty\":1,"
                        + "\"major\":\"m\",\"birthday\":{\"year\":2000,\"month\":2,\"date\":3},"
                        + "\"location\":{\"region\":\"CN\",\"state\":\"44\",\"city\":\"1\"},"
                        + "\"hometown\":null}",
                ProfilePatchRequest.class);

        Map<String, Object> patch = request.toPatchMap();
        assertEquals("n", patch.get("nickname"));
        assertEquals("i", patch.get("introduction"));
        assertEquals(2024, patch.get("enrollment"));
        assertEquals(1, patch.get("faculty"));
        assertEquals("m", patch.get("major"));
        assertEquals(Map.of("year", 2000, "month", 2, "date", 3), patch.get("birthday"));
        assertEquals(Map.of("region", "CN", "state", "44", "city", "1"), patch.get("location"));
        assertTrue(patch.containsKey("hometown"));
        assertNull(patch.get("hometown"));
    }

    @Test
    void fractionalNumbersAreRejectedInsteadOfTruncated() {
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"enrollment\":2024.9}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"faculty\":1.5}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"birthday\":{\"year\":2000.5,\"month\":1,\"date\":2}}", ProfilePatchRequest.class));
    }

    @Test
    void nonIntegerTokensAreRejectedInsteadOfCoerced() {
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"enrollment\":\"2024\"}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"enrollment\":\"\"}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"birthday\":{\"year\":\"2000\",\"month\":1,\"date\":2}}", ProfilePatchRequest.class));
    }

    @Test
    void nonStringTokensAreRejectedInsteadOfCoerced() {
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"nickname\":123}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"introduction\":false}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"major\":42}", ProfilePatchRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"location\":{\"region\":\"CN\",\"state\":44}}", ProfilePatchRequest.class));
    }
}
