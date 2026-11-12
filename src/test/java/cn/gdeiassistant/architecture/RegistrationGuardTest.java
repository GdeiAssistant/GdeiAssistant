package cn.gdeiassistant.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two registration lists were plain strings, so a new module could be added without
 * the compiler noticing: @MapperScan basePackages and the community query-log pointcut.
 * These tests tie both lists to what is actually on disk / on the classpath.
 */
class RegistrationGuardTest {

    private static final Path SRC = Paths.get("src/main/java");
    private static final Path CORE = SRC.resolve("cn/gdeiassistant/core");
    private static final List<String> DATA_SOURCE_CONFIGS = List.of(
            "AppDataSourceConfig", "DataDataSourceConfig", "LogDataSourceConfig");

    private static final List<String> COMMUNITY_MODULES = List.of(
            "secret", "topic", "express", "marketplace",
            "lostandfound", "delivery", "dating", "photograph");

    private static String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static Set<String> mapperPackagesOnDisk() throws IOException {
        Set<String> result = new TreeSet<>();
        try (Stream<Path> files = Files.walk(CORE)) {
            Iterable<Path> iterable = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.getParent().getFileName().toString().equals("mapper"))::iterator;
            for (Path file : iterable) {
                result.add(SRC.relativize(file.getParent()).toString().replace('/', '.'));
            }
        }
        return result;
    }

    private static List<String> registeredMapperPackages() {
        List<String> result = new ArrayList<>();
        for (String name : DATA_SOURCE_CONFIGS) {
            String source = read(SRC.resolve(
                    "cn/gdeiassistant/common/config/datasource/" + name + ".java"));
            Matcher m = Pattern.compile("@MapperScan\\(basePackages\\s*=\\s*\\{(.*?)\\}",
                    Pattern.DOTALL).matcher(source);
            assertTrue(m.find(), "Cannot find @MapperScan in " + name);
            Matcher pkg = Pattern.compile("\"([^\"]+)\"").matcher(m.group(1));
            while (pkg.find()) {
                result.add(pkg.group(1));
            }
        }
        return result;
    }

    @Test
    void everyMapperPackageIsAssignedToExactlyOneDataSource() throws IOException {
        Set<String> disk = mapperPackagesOnDisk();
        List<String> registered = registeredMapperPackages();
        assertEquals(disk.size(), registered.size(),
                disk.size() + " mapper packages found on disk, but a different number is registered");
        assertEquals(new HashSet<>(registered).size(), registered.size(),
                "A mapper package must not be scanned by two data sources");
        assertEquals(disk, new TreeSet<>(registered),
                "Every core/**/mapper package must be registered in exactly one @MapperScan");
    }

    @Test
    void mapperPackagesContainOnlyMyBatisInterfaces() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(CORE)) {
            Iterable<Path> iterable = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.getParent().getFileName().toString().equals("mapper"))::iterator;
            for (Path file : iterable) {
                String source = read(file);
                String withoutComments = source.replaceAll("/\\*.*?\\*/|//[^\\n]*", "");
                if (!Pattern.compile("public\\s+interface\\s+\\w+").matcher(withoutComments).find()) {
                    offenders.add(SRC.relativize(file).toString());
                }
            }
        }
        assertTrue(offenders.isEmpty(),
                "Only MyBatis mapper interfaces belong in a mapper package: " + offenders);
    }

    @Test
    void communityQueryLogPointcutMatchesTheControllersOnDisk() throws Exception {
        String aspect = read(SRC.resolve("cn/gdeiassistant/common/aspect/QueryLogAspect.java"));
        Matcher m = Pattern.compile(
                "execution\\(\\*\\s+(cn\\.gdeiassistant\\.core\\.[A-Za-z0-9_.]+)\\.\\*\\(\\.\\.\\)\\)")
                .matcher(aspect);
        Set<String> pointcut = new TreeSet<>();
        while (m.find()) {
            String fqn = m.group(1);
            Class.forName(fqn); // a renamed or misspelled controller fails here, not in production
            pointcut.add(fqn);
        }

        Set<String> onDisk = new TreeSet<>();
        for (String module : COMMUNITY_MODULES) {
            Path dir = CORE.resolve(module + "/controller");
            try (Stream<Path> files = Files.walk(dir, 1)) {
                Iterable<Path> iterable = files
                        .filter(p -> p.getFileName().toString().endsWith("Controller.java"))::iterator;
                for (Path file : iterable) {
                    onDisk.add("cn.gdeiassistant.core." + module + ".controller." + file.getFileName()
                            .toString().replace(".java", ""));
                }
            }
        }
        assertEquals(onDisk, pointcut,
                "Every community controller must be covered by QueryLogAspect.communityQueryAction()");
    }
}
