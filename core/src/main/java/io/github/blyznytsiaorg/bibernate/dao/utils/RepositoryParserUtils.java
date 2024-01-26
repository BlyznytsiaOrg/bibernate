package io.github.blyznytsiaorg.bibernate.dao.utils;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import io.github.blyznytsiaorg.bibernate.annotation.Param;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.*;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@UtilityClass
public class RepositoryParserUtils {

    private static final Paranamer info = new CachingParanamer(new ParamAnnotationParanamer(new BytecodeReadingParanamer()));

    private static final Set<String> SUPPORTED_OPERATIONS = new HashSet<>(Set.of(
            "And", "Or", "Equals", "Like",
            "Null", "Notnull", "Lessthan",
            "Lessthanequal", "Greaterthan",  "Greaterthanequal"
    ));

    private static final Map<String, String> OPERATION_TO_SQL_CONDITIONS = new HashMap<>();
    public static final String EQ = " = ";
    public static final String PARAMETER = "?";
    public static final String UNDERSCORE = "_";

    static {
        OPERATION_TO_SQL_CONDITIONS.put("And", " = ? And ");
        OPERATION_TO_SQL_CONDITIONS.put("Or", " = ? Or ");
        OPERATION_TO_SQL_CONDITIONS.put("Equals", " = ?");
        OPERATION_TO_SQL_CONDITIONS.put("Lessthan", " < ?");
        OPERATION_TO_SQL_CONDITIONS.put("Lessthanequal", " <= ?");
        OPERATION_TO_SQL_CONDITIONS.put("Greaterthan", " > ?");
        OPERATION_TO_SQL_CONDITIONS.put("Greaterthanequal", " >= ?");
        OPERATION_TO_SQL_CONDITIONS.put("Null", " is null");
        OPERATION_TO_SQL_CONDITIONS.put("Notnull", " is not null");
        OPERATION_TO_SQL_CONDITIONS.put("Like", " like ?");
    }

    private static final String EMPTY = "";
    private static final String FIND_BY = "findBy";
    public static final String NAME_SPLITERATOR = "(?=\\p{Upper})";

    public static String buildQueryByMethodName(String methodName) {
        methodName = methodName.replace(FIND_BY, EMPTY);
        String[] methodSplit = methodName.split(NAME_SPLITERATOR);
        Queue<String> fields = new ArrayDeque<>();
        Queue<String> operations = new ArrayDeque<>();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < methodSplit.length; i++) {
            String partName = methodSplit[i];

            if (!SUPPORTED_OPERATIONS.contains(partName)) {
                builder.append(partName);
            } else {
                fields.add(builder.toString());
                builder = new StringBuilder();
                operations.add(partName);
            }

            if (i == methodSplit.length - 1) {
                String lastPart = builder.toString();
                if (!lastPart.isEmpty()) {
                    fields.add(lastPart);
                }
            }
        }

        log.debug("fields {} operations {}", fields, operations);

        var whereQuery = new StringBuilder();
        while (!fields.isEmpty()) {
            String currentField = fields.poll();
            String convertToUnderscoreField = convertToUnderscore(currentField);
            whereQuery.append(convertToUnderscoreField);

            String currentOperation = operations.poll();
            if (currentOperation != null) {
                String sqlOperation = OPERATION_TO_SQL_CONDITIONS.get(currentOperation);
                whereQuery.append(sqlOperation);
            } else {
                if (!whereQuery.isEmpty()) {
                    whereQuery.append(EQ).append(PARAMETER);
                }
            }
        }

        log.debug("where " + whereQuery);
        return whereQuery.toString();
    }

    public static String convertToUnderscore(String originalColumnName) {
        var result = new StringBuilder();
        for (int i = 0; i < originalColumnName.length(); i++) {
            char currentChar = originalColumnName.charAt(i);
            if (isUpperCase(currentChar)) {
                if (i > 0) {
                    result.append(UNDERSCORE);
                }
                result.append(toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    /**
     * Retrieves parameter names of a method or constructor.
     *
     * @param methodOrConstructor The method or constructor to retrieve parameter names from
     * @return A list of parameter names
     */
    public static List<String> getParameterNames(AccessibleObject methodOrConstructor) {
        String[] parameterNames = info.lookupParameterNames(methodOrConstructor, false);
        if (parameterNames.length == 0) {
            log.info("Method {} don't have parameters", methodOrConstructor);
            return Collections.emptyList();
        }
        return Arrays.stream(parameterNames).toList();
    }

    private static class ParamAnnotationParanamer extends AnnotationParanamer {

        public ParamAnnotationParanamer(Paranamer fallback) {
            super(fallback);
        }

        @Override
        protected String getNamedValue(Annotation ann) {
            if (Objects.equals(Param.class, ann.annotationType())) {
                Param param = (Param) ann;
                return param.value();
            } else {
                return null;
            }
        }

        @Override
        protected boolean isNamed(Annotation ann) {
            return Objects.equals(Param.class, ann.annotationType());
        }
    }
}
