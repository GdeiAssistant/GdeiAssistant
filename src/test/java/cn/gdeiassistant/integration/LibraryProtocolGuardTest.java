package cn.gdeiassistant.integration;

import cn.gdeiassistant.core.bookquery.service.BookQueryService;
import cn.gdeiassistant.integration.library.pojo.LibraryRenewResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the boundary between the OPAC anti-corruption layer and the library domain.
 * The upstream Chinese rejection text used to be a business condition inside
 * BookQueryService, so any change of wording upstream silently broke renewals.
 */
class LibraryProtocolGuardTest {

    private static final String UPSTREAM_MARKER = "超过最大续借次数";

    @Test
    void upstreamMessageIsOnlyKnownInsideTheAntiCorruptionLayer() throws IOException {
        Path main = Paths.get("src/main/java");
        Path owner = main.resolve("cn/gdeiassistant/integration/library/pojo/LibraryRenewResult.java");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(main)) {
            Iterable<Path> iterable = files.filter(p -> p.toString().endsWith(".java"))::iterator;
            for (Path file : iterable) {
                if (file.equals(owner)) {
                    continue;
                }
                if (Files.readString(file).contains(UPSTREAM_MARKER)) {
                    offenders.add(file.toString());
                }
            }
        }
        assertTrue(offenders.isEmpty(), "Upstream OPAC text leaked into business code: " + offenders);
    }

    @Test
    void fromOpacClassifiesOutcomesWithoutExactMessageMatching() {
        assertEquals(LibraryRenewResult.Outcome.SUCCESS,
                LibraryRenewResult.fromOpac(1, "ok").getOutcome());
        assertEquals(LibraryRenewResult.Outcome.RENEWAL_LIMIT,
                LibraryRenewResult.fromOpac(1, "超过最大续借次数:1！").getOutcome());
        assertEquals(LibraryRenewResult.Outcome.RENEWAL_LIMIT,
                LibraryRenewResult.fromOpac(1, "超过最大续借次数:2！").getOutcome());
        assertEquals(LibraryRenewResult.Outcome.FAILURE,
                LibraryRenewResult.fromOpac(0, "ok").getOutcome());
    }

    @Test
    void bookQueryServiceHasNoForwardingDuplicates() {
        assertThrows(NoSuchMethodException.class, () -> BookQueryService.class
                .getMethod("bookRenew", String.class, String.class, String.class));
        assertThrows(NoSuchMethodException.class, () -> BookQueryService.class
                .getMethod("bookquery", String.class, String.class));
    }
}
