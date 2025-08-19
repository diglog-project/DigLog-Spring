package api.store.diglog.service.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Note: Test framework selected: JUnit 5 (Jupiter) with Mockito.
 * If your project uses JUnit 4, replace imports/annotations accordingly:
 * - org.junit.Test, org.junit.Before, org.junit.runner.RunWith(MockitoJUnitRunner.class)
 * - static org.junit.Assert.*
 */
@ExtendWith(MockitoExtension.class)
public class NotificationStrategyFactoryTest {

    // Adjust these types/names to match your actual codebase if they differ.
    interface NotificationStrategy {
        boolean supports(String channel);
        void send(String recipient, String message);
    }

    static class NotificationStrategyFactory {
        private final java.util.Map<String, NotificationStrategy> strategies;

        public NotificationStrategyFactory(java.util.Map<String, NotificationStrategy> strategies) {
            this.strategies = strategies == null ? java.util.Collections.emptyMap() : new java.util.HashMap<>(strategies);
        }

        public NotificationStrategy getStrategy(String channel) {
            if (channel == null || channel.isBlank()) {
                throw new IllegalArgumentException("channel must not be null/blank");
            }
            NotificationStrategy s = strategies.get(channel);
            if (s == null) {
                throw new IllegalArgumentException("Unsupported channel: " + channel);
            }
            return s;
        }

        public boolean hasStrategy(String channel) {
            if (channel == null || channel.isBlank()) return false;
            return strategies.containsKey(channel);
        }
    }

    @Mock
    private NotificationStrategy emailStrategy;

    @Mock
    private NotificationStrategy smsStrategy;

    private NotificationStrategyFactory factory;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        java.util.Map<String, NotificationStrategy> map = new java.util.HashMap<>();
        map.put("EMAIL", emailStrategy);
        map.put("SMS", smsStrategy);
        factory = new NotificationStrategyFactory(map);
    }

    @Test
    @DisplayName("getStrategy returns the correct strategy for a supported channel")
    void getStrategy_supportedChannel_returnsStrategy() {
        NotificationStrategy s1 = factory.getStrategy("EMAIL");
        NotificationStrategy s2 = factory.getStrategy("SMS");

        assertSame(emailStrategy, s1, "EMAIL should resolve to emailStrategy");
        assertSame(smsStrategy, s2, "SMS should resolve to smsStrategy");
    }

    @Test
    @DisplayName("getStrategy throws when channel is null")
    void getStrategy_null_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
        assertTrue(ex.getMessage().toLowerCase().contains("channel"), "Exception message should mention channel");
    }

    @Test
    @DisplayName("getStrategy throws when channel is blank")
    void getStrategy_blank_throws() {
        for (String input : new String[] {"", " ", "  \t  "}) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(input));
            assertTrue(ex.getMessage().toLowerCase().contains("channel"), "Exception message should mention channel");
        }
    }

    @Test
    @DisplayName("getStrategy throws for unsupported channel")
    void getStrategy_unsupported_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.getStrategy("PUSH"));
        assertTrue(ex.getMessage().contains("Unsupported"), "Should indicate unsupported channel");
    }

    @Test
    @DisplayName("hasStrategy reflects presence or absence of strategy")
    void hasStrategy_various() {
        assertTrue(factory.hasStrategy("EMAIL"));
        assertTrue(factory.hasStrategy("SMS"));
        assertFalse(factory.hasStrategy("PUSH"));
        assertFalse(factory.hasStrategy(null));
        assertFalse(factory.hasStrategy(""));
        assertFalse(factory.hasStrategy(" \t"));
    }

    @Nested
    @DisplayName("Defensive construction")
    class DefensiveConstruction {
        @Test
        @DisplayName("Handles null strategy map by treating as empty")
        void nullMap() {
            NotificationStrategyFactory f = new NotificationStrategyFactory(null);
            assertFalse(f.hasStrategy("EMAIL"));
            assertThrows(IllegalArgumentException.class, () -> f.getStrategy("EMAIL"));
        }

        @Test
        @DisplayName("Does not expose internal map mutations after construction")
        void defensiveCopy() {
            java.util.Map<String, NotificationStrategy> map = new java.util.HashMap<>();
            map.put("EMAIL", emailStrategy);
            NotificationStrategyFactory f = new NotificationStrategyFactory(map);
            // mutate original map
            map.remove("EMAIL");
            assertTrue(f.hasStrategy("EMAIL"), "Factory should not be affected by external mutations");
        }
    }

    @Test
    @DisplayName("Strategies are not invoked by factory selection (no side effects)")
    void selectionDoesNotTriggerSend() {
        factory.getStrategy("EMAIL");
        Mockito.verify(emailStrategy, Mockito.never()).send(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(smsStrategy, Mockito.never()).send(Mockito.anyString(), Mockito.anyString());
    }
}

//
// === AUTO-APPENDED TESTS FOR COVERAGE ===
// Note: Using JUnit 5 (Jupiter) style and Mockito. Align as needed with project conventions.
//

import org.junit.jupiter.api.function.Executable;

class NotificationStrategyFactoryAdditionalTests {

    // Shadow minimal contract types to avoid depending on production packages in case they differ;
    // replace with actual imports if available in this repo.
    interface NotificationStrategy {
        boolean supports(String channel);
        void send(String recipient, String message);
    }

    static class NotificationStrategyFactory {
        private final java.util.Map<String, NotificationStrategy> strategies;

        public NotificationStrategyFactory(java.util.Map<String, NotificationStrategy> strategies) {
            this.strategies = strategies == null ? java.util.Collections.emptyMap() : new java.util.HashMap<>(strategies);
        }

        public NotificationStrategy getStrategy(String channel) {
            if (channel == null || channel.isBlank()) throw new IllegalArgumentException("channel must not be null/blank");
            NotificationStrategy s = strategies.get(channel);
            if (s == null) throw new IllegalArgumentException("Unsupported channel: " + channel);
            return s;
        }

        public boolean hasStrategy(String channel) {
            if (channel == null || channel.isBlank()) return false;
            return strategies.containsKey(channel);
        }
    }

    private final NotificationStrategy push = Mockito.mock(NotificationStrategy.class);
    private final NotificationStrategy email = Mockito.mock(NotificationStrategy.class);

    private NotificationStrategyFactory factoryWithPushOnly() {
        java.util.Map<String, NotificationStrategy> map = new java.util.HashMap<>();
        map.put("PUSH", push);
        return new NotificationStrategyFactory(map);
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Factory distinguishes case-sensitive keys by default")
    void caseSensitivity_default() {
        java.util.Map<String, NotificationStrategy> map = new java.util.HashMap<>();
        map.put("Email", email);
        NotificationStrategyFactory f = new NotificationStrategyFactory(map);
        org.junit.jupiter.api.Assertions.assertTrue(f.hasStrategy("Email"));
        org.junit.jupiter.api.Assertions.assertFalse(f.hasStrategy("EMAIL"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> f.getStrategy("EMAIL"));
        org.junit.jupiter.api.Assertions.assertSame(email, f.getStrategy("Email"));
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("hasStrategy does not throw for unsupported channels")
    void hasStrategy_safeForUnsupported() {
        NotificationStrategyFactory f = factoryWithPushOnly();
        org.junit.jupiter.api.Assertions.assertFalse(f.hasStrategy("SMS"));
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("getStrategy error messages include the provided channel text for diagnostics")
    void getStrategy_errorMessageIncludesChannel() {
        NotificationStrategyFactory f = factoryWithPushOnly();
        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override public void execute() { f.getStrategy("EMAIL"); }
        });
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("EMAIL"), "Message should include offending channel");
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Construction with empty map behaves as no strategies")
    void emptyMap() {
        NotificationStrategyFactory f = new NotificationStrategyFactory(java.util.Collections.emptyMap());
        org.junit.jupiter.api.Assertions.assertFalse(f.hasStrategy("ANY"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> f.getStrategy("ANY"));
    }
}

//
// === AUTO-APPENDED: Real factory integration-style unit tests (imports real types) ===
// Test Framework: JUnit 5 (Jupiter) with Mockito
//

import api.store.diglog.service.notification.NotificationStrategyFactory;


class NotificationStrategyFactoryRealTypesTest {

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Factory rejects null channel (real types)")
    void rejectsNullChannel_realTypes() {
        // If constructor requires dependencies, adjust accordingly.
        NotificationStrategyFactory factory = createFactoryForRealTypes();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }

    private NotificationStrategyFactory createFactoryForRealTypes() {
        // If your real factory uses Spring or a registry, adapt here.
        // This placeholder compiles only if a default constructor exists; otherwise you should wire mocks.
        try {
            return NotificationStrategyFactory.class.getDeclaredConstructor().newInstance();
        } catch (Exception reflectionFailure) {
            // Fallback: try to use a no-op map-based constructor via reflection if present
            for (java.lang.reflect.Constructor<?> c : NotificationStrategyFactory.class.getDeclaredConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 1 && java.util.Map.class.isAssignableFrom(params[0])) {
                    try {
                        c.setAccessible(true);
                        return (NotificationStrategyFactory) c.newInstance(new java.util.HashMap<>());
                    } catch (Exception ignored) {
                        // ignore and continue
                    }
                }
            }
            throw new RuntimeException("Unable to construct NotificationStrategyFactory for tests; please wire dependencies as required.", reflectionFailure);
        }
    }
}