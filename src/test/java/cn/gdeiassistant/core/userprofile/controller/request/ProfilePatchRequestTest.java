package cn.gdeiassistant.core.userprofile.controller.request;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePatchRequestTest {

    private final JsonMapper mapper = new JsonMapper();

    @Test
    void absentFieldsAreNotMarkedProvided() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"nickname\":\"new\"}", ProfilePatchRequest.class);

        assertTrue(request.hasField("nickname"));
        assertEquals("new", request.getNickname());
        assertFalse(request.hasField("introduction"));
        assertFalse(request.hasField("birthday"));
        assertFalse(request.hasField("enrollment"));
        assertFalse(request.hasField("faculty"));
        assertFalse(request.hasField("major"));
        assertFalse(request.hasField("location"));
        assertFalse(request.hasField("hometown"));
        assertTrue(request.getUnknownFields().isEmpty());
    }

    @Test
    void explicitNullIsMarkedProvidedForClearing() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"birthday\":null,\"introduction\":null}", ProfilePatchRequest.class);

        assertTrue(request.hasField("birthday"));
        assertNull(request.getBirthday());
        assertTrue(request.hasField("introduction"));
        assertNull(request.getIntroduction());
        assertFalse(request.hasField("nickname"));
    }

    @Test
    void nestedObjectsKeepTheirTypedShape() throws Exception {
        ProfilePatchRequest request = mapper.readValue(
                "{\"birthday\":{\"year\":2000,\"month\":1,\"date\":2},"
                        + "\"location\":{\"region\":\"CN\",\"state\":\"44\",\"city\":\"1\"}}",
                ProfilePatchRequest.class);

        assertTrue(request.hasField("birthday"));
        assertNotNull(request.getBirthday());
        assertEquals(2000, request.getBirthday().getYear());
        assertEquals(1, request.getBirthday().getMonth());
        assertEquals(2, request.getBirthday().getDate());

        assertTrue(request.hasField("location"));
        assertNotNull(request.getLocation());
        assertEquals("CN", request.getLocation().getRegion());
        assertEquals("44", request.getLocation().getState());
        assertEquals("1", request.getLocation().getCity());
    }

    @Test
    void unknownFieldsReachTheServiceForRejection() throws Exception {
        ProfilePatchRequest request = mapper.readValue("{\"username\":\"bob\"}", ProfilePatchRequest.class);

        assertEquals(Map.of("username", "bob"), request.getUnknownFields());
        assertFalse(request.hasField("username"));
    }

    @Test
    void everySupportedFieldRoundTrips() throws Exception {
        ProfilePatchRequest request = mapper.readValue(
                "{\"nickname\":\"n\",\"introduction\":\"i\",\"enrollment\":2024,\"faculty\":1,"
                        + "\"major\":\"m\",\"birthday\":{\"year\":2000,\"month\":2,\"date\":3},"
                        + "\"location\":{\"region\":\"CN\",\"state\":\"44\",\"city\":\"1\"},"
                        + "\"hometown\":null}",
                ProfilePatchRequest.class);

        assertTrue(request.hasField("nickname"));
        assertEquals("n", request.getNickname());
        assertTrue(request.hasField("introduction"));
        assertEquals("i", request.getIntroduction());
        assertTrue(request.hasField("enrollment"));
        assertEquals(2024, request.getEnrollment());
        assertTrue(request.hasField("faculty"));
        assertEquals(1, request.getFaculty());
        assertTrue(request.hasField("major"));
        assertEquals("m", request.getMajor());
        assertTrue(request.hasField("birthday"));
        assertEquals(2000, request.getBirthday().getYear());
        assertEquals(2, request.getBirthday().getMonth());
        assertEquals(3, request.getBirthday().getDate());
        assertTrue(request.hasField("location"));
        assertEquals("CN", request.getLocation().getRegion());
        assertEquals("44", request.getLocation().getState());
        assertEquals("1", request.getLocation().getCity());
        assertTrue(request.hasField("hometown"));
        assertNull(request.getHometown());
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
