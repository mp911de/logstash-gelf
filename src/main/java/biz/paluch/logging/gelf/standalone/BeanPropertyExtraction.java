package biz.paluch.logging.gelf.standalone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Paluch
 * @since 31.07.14 08:56
 */
class BeanPropertyExtraction {
    public static final String IS_REGEX = "is([^ ]+)";
    public static final String GET_REGEX = "get([^ ]+)";

    public static final Pattern IS_PATTERN = Pattern.compile(IS_REGEX);
    public static final Pattern GET_PATTERN = Pattern.compile(GET_REGEX);

    static Map<String, Object> extractProperties(Object object) {

        Map<String, Method> methodMap = createPropertyToMethodMap(object);

        Map<String, Object> fields = retrieveValues(object, methodMap);
        return fields;
    }

    private static Map<String, Method> createPropertyToMethodMap(Object object) {
        Class<?> beanClass = object.getClass();
        Map<String, Method> methodMap = new HashMap<String, Method>();

        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {

            if (method.getParameterTypes().length != 0) {
                continue;
            }

            if (method.getReturnType().equals(Void.class) || method.getReturnType().equals(Void.TYPE)) {
                continue;
            }

            if (method.getDeclaringClass().equals(Object.class)) {
                continue;
            }

            String name = method.getName();
            String propertyName = getPropertyName(name, GET_PATTERN);
            if (propertyName == null) {
                propertyName = getPropertyName(name, IS_PATTERN);
            }

            if (propertyName == null) {
                continue;
            }

            propertyName = decapitalizePropertyName(propertyName);

            methodMap.put(propertyName, method);
        }
        return methodMap;
    }

    private static String decapitalizePropertyName(String propertyName) {
        String first = propertyName.substring(0, 1);
        String last = propertyName.substring(1);

        return first.toLowerCase() + last;

    }

    private static Map<String, Object> retrieveValues(Object object, Map<String, Method> methodMap) {
        Map<String, Object> fields = new HashMap<String, Object>();

        for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
            try {
                Object value = entry.getValue().invoke(object);

                if (value != null) {
                    fields.put(entry.getKey(), value);
                }

            } catch (IllegalAccessException e) {
                // ignore
            } catch (InvocationTargetException e) {
                // ignore
            }

        }
        return fields;
    }

    private static String getPropertyName(String name, Pattern pattern) {
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            return matcher.toMatchResult().group(1);
        }
        return null;
    }
}
