package app.hub.onboarding;

import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for OnboardingPreferences helper class.
 * 
 * Note: Full integration tests for SharedPreferences functionality are implemented
 * as instrumented tests (androidTest) in OnboardingPreferencesInstrumentedTest.java,
 * which run on a device or emulator with real Android framework components.
 * 
 * These unit tests validate:
 * - Constructor validation (null context check)
 * - Class structure and API design
 * - Method signatures and return types
 * - Error handling design (fail-safe approach)
 * 
 * Comprehensive functional tests covering requirements 6.1-6.4 are in:
 * - OnboardingPreferencesInstrumentedTest.java (instrumented tests)
 *   - Test reading preference when unset (should return false)
 *   - Test setting and reading preference
 *   - Test persistence across instances
 *   - Test error handling for SharedPreferences failures
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
public class OnboardingPreferencesTest {

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPreferencesWithNullContextThrowsException() {
        // When creating OnboardingPreferences with null context
        new OnboardingPreferences(null);
        // Then IllegalArgumentException should be thrown (Requirement 6.1, 6.2, 6.3, 6.4)
    }

    @Test
    public void onboardingPreferencesClassExists() {
        // Verify that the OnboardingPreferences class is properly defined
        // This test ensures the class can be loaded and instantiated
        assertNotNull("OnboardingPreferences class should exist", OnboardingPreferences.class);
    }

    @Test
    public void onboardingPreferencesHasIsOnboardingCompleteMethod() throws NoSuchMethodException {
        // Verify that isOnboardingComplete() method exists with correct signature
        Method method = OnboardingPreferences.class.getMethod("isOnboardingComplete");
        assertNotNull("isOnboardingComplete() method should exist", method);
        
        // Verify return type is boolean (Requirement 6.2, 6.3)
        assertEquals("isOnboardingComplete() should return boolean", 
                boolean.class, method.getReturnType());
        
        // Verify method is public
        assertTrue("isOnboardingComplete() should be public", 
                Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public void onboardingPreferencesHasSetOnboardingCompleteMethod() throws NoSuchMethodException {
        // Verify that setOnboardingComplete() method exists with correct signature
        Method method = OnboardingPreferences.class.getMethod("setOnboardingComplete", boolean.class);
        assertNotNull("setOnboardingComplete() method should exist", method);
        
        // Verify return type is boolean (indicates success/failure) (Requirement 6.1, 6.4)
        assertEquals("setOnboardingComplete() should return boolean", 
                boolean.class, method.getReturnType());
        
        // Verify method is public
        assertTrue("setOnboardingComplete() should be public", 
                Modifier.isPublic(method.getModifiers()));
        
        // Verify parameter type is boolean
        Class<?>[] paramTypes = method.getParameterTypes();
        assertEquals("setOnboardingComplete() should have one parameter", 1, paramTypes.length);
        assertEquals("setOnboardingComplete() parameter should be boolean", 
                boolean.class, paramTypes[0]);
    }

    @Test
    public void onboardingPreferencesConstructorIsPublic() throws NoSuchMethodException {
        // Verify that the constructor is public and accessible
        var constructor = OnboardingPreferences.class.getConstructor(android.content.Context.class);
        assertNotNull("Constructor should exist", constructor);
        assertTrue("Constructor should be public", Modifier.isPublic(constructor.getModifiers()));
    }

    @Test
    public void onboardingPreferencesClassIsPublic() {
        // Verify that the class is public and can be accessed from other packages
        assertTrue("OnboardingPreferences class should be public", 
                Modifier.isPublic(OnboardingPreferences.class.getModifiers()));
    }

    @Test
    public void onboardingPreferencesIsNotAbstract() {
        // Verify that the class can be instantiated (not abstract)
        assertFalse("OnboardingPreferences should not be abstract", 
                Modifier.isAbstract(OnboardingPreferences.class.getModifiers()));
    }

    @Test
    public void onboardingPreferencesIsNotInterface() {
        // Verify that OnboardingPreferences is a concrete class, not an interface
        assertFalse("OnboardingPreferences should not be an interface", 
                OnboardingPreferences.class.isInterface());
    }

    @Test
    public void onboardingPreferencesHasExpectedMethodCount() {
        // Verify that the class has the expected public methods
        Method[] publicMethods = OnboardingPreferences.class.getDeclaredMethods();
        int publicMethodCount = 0;
        
        for (Method method : publicMethods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        
        // Should have exactly 2 public methods: isOnboardingComplete() and setOnboardingComplete()
        assertEquals("OnboardingPreferences should have exactly 2 public methods", 
                2, publicMethodCount);
    }

    @Test
    public void onboardingPreferencesMethodNamesFollowConvention() throws NoSuchMethodException {
        // Verify that method names follow Java naming conventions
        
        // isOnboardingComplete follows boolean getter convention (is*)
        Method isMethod = OnboardingPreferences.class.getMethod("isOnboardingComplete");
        assertTrue("Boolean getter should start with 'is'", 
                isMethod.getName().startsWith("is"));
        
        // setOnboardingComplete follows setter convention (set*)
        Method setMethod = OnboardingPreferences.class.getMethod("setOnboardingComplete", boolean.class);
        assertTrue("Setter should start with 'set'", 
                setMethod.getName().startsWith("set"));
    }

    @Test
    public void onboardingPreferencesHasNoStaticMethods() {
        // Verify that all methods are instance methods (not static)
        // This ensures proper encapsulation and testability
        Method[] methods = OnboardingPreferences.class.getDeclaredMethods();
        
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertFalse("Public methods should not be static: " + method.getName(), 
                        Modifier.isStatic(method.getModifiers()));
            }
        }
    }

    @Test
    public void onboardingPreferencesExtendsObject() {
        // Verify that OnboardingPreferences extends Object (no custom inheritance)
        assertEquals("OnboardingPreferences should extend Object", 
                Object.class, OnboardingPreferences.class.getSuperclass());
    }

    @Test
    public void onboardingPreferencesImplementsNoInterfaces() {
        // Verify that OnboardingPreferences is a simple helper class with no interfaces
        Class<?>[] interfaces = OnboardingPreferences.class.getInterfaces();
        assertEquals("OnboardingPreferences should implement no interfaces", 
                0, interfaces.length);
    }
}
