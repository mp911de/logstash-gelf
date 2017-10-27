package external;

import static org.mockito.Mockito.*;

import java.lang.reflect.Parameter;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * {@code MockitoExtension} showcases the {@link TestInstancePostProcessor} and {@link ParameterResolver} extension APIs of
 * JUnit 5 by providing dependency injection support at the field level and at the method parameter level via Mockito 2.x's
 * {@link Mock @Mock} annotation.
 */
public class MockitoExtension implements TestInstancePostProcessor, ParameterResolver {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        MockitoAnnotations.initMocks(testInstance);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(Mock.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getMock(parameterContext.getParameter(), extensionContext);
    }

    private Object getMock(Parameter parameter, ExtensionContext extensionContext) {
        final Class<?> mockType = parameter.getType();
        final Store mocks = extensionContext.getStore(Namespace.create(MockitoExtension.class, mockType));
        final String mockName = getMockName(parameter);

        if (mockName != null) {
            return mocks.getOrComputeIfAbsent(mockName, new Function<String, Object>() {
                @Override
                public Object apply(String s) {
                    return mock(mockType, mockName);
                }
            });
        } else {
            return mocks.getOrComputeIfAbsent(mockType.getCanonicalName(), new Function<String, Object>() {
                @Override
                public Object apply(String s) {
                    return mock(mockType, mockName);
                }
            });
        }
    }

    private String getMockName(final Parameter parameter) {
        String explicitMockName = parameter.getAnnotation(Mock.class).name().trim();
        if (!explicitMockName.isEmpty()) {
            return explicitMockName;
        } else if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        return null;
    }

}