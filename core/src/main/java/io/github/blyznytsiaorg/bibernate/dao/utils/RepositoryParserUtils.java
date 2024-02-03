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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 * Utility class for parsing repository method names and building corresponding SQL WHERE queries.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@UtilityClass
public class RepositoryParserUtils {
    /**
     * Paranamer instance for retrieving parameter names with consideration of annotations.
     */
    private static final Paranamer info = new CachingParanamer(new ParamAnnotationParanamer(new BytecodeReadingParanamer()));
    /**
     * Set of supported operations for constructing SQL queries.
     */
    private static final Set<String> SUPPORTED_OPERATIONS = Set.of(
            "And", "Or", "Equals", "Like", "Null", "NotNull", "LessThan",
            "LessThanEqual", "GreaterThan", "GreaterThanEqual"
    );

    /**
     * Mapping of supported operations to their corresponding SQL conditions.
     */
    private static final Map<String, String> OPERATION_TO_SQL_CONDITIONS = Map.of(
            "And", " = ? And ",
            "Or", " = ? Or ",
            "Equals", " = ?",
            "LessThan", " < ?",
            "LessThanEqual", " <= ?",
            "GreaterThan", " > ?",
            "GreaterThanEqual", " >= ?",
            "Null", " is null",
            "NotNull", " is not null",
            "Like", " like ?"
    );

    /**
     * Set of partial SQL conditions used in method name parsing.
     */
    private static final Set<String> PART_SQL_CONDITIONS = Set.of("Less", "Than", "Equal", "Greater", "Not", "Null");
    /**
     * String representing the equality operation in SQL.
     */
    private static final String EQ = " = ";
    /**
     * Placeholder for parameter in SQL queries.
     */
    private static final String PARAMETER = "?";
    /**
     * Underscore character used for converting camel case to underscore format.
     */
    private static final String UNDERSCORE = "_";
    /**
     * Message indicating that a method doesn't have parameters annotated with @Param.
     */
    private static final String WILL_RESOLVE_AS_REGULAR_METHOD_PARAMETERS =
            "Method {} don't have parameters that annotation @Param. Will resolve as regular method parameters";
    /**
     * Empty string constant.
     */
    private static final String EMPTY = "";
    /**
     * Prefix indicating the start of a find-by method in the method name.
     */
    private static final String FIND_BY = "findBy";
    /**
     * Regular expression for splitting method names based on camel case.
     */
    private static final String NAME_SPLITERATOR = "(?=\\p{Upper})";

    /**
     * Prefix for log messages related to constructing WHERE queries.
     */
    private static final String WHERE = "Where query ";

    /**
     * Log message format for debug messages containing field and operation information.
     */
    private static final String FIELDS_OPERATIONS = "Fields {} operations {}";

    /**
     * Builds a WHERE query based on the provided repository method name.
     *
     * @param methodName The repository method name.
     * @return The constructed WHERE query.
     */
    public static String buildQueryByMethodName(String methodName) {
        methodName = methodName.replace(FIND_BY, EMPTY);
        String[] methodSplit = methodName.split(NAME_SPLITERATOR);
        Queue<String> fields = new ArrayDeque<>();
        Queue<String> operations = new ArrayDeque<>();

        var field = new StringBuilder();
        var operation = new StringBuilder();

        for (var partName : methodSplit) {
            if (PART_SQL_CONDITIONS.contains(partName)) {
                operation.append(partName);
            } else {
                if (SUPPORTED_OPERATIONS.contains(partName)) {
                    addFieldAndOperation(fields, operations, field, operation, partName);
                } else {
                    field.append(partName);
                }
            }
        }

        addRemainingFieldsAndOperations(fields, operations, field, operation);

        log.debug(FIELDS_OPERATIONS, fields, operations);

        return buildWhereQuery(fields, operations);
    }

    /**
     * Adds the current field and operation to the corresponding queues and resets the StringBuilders.
     * If the current operation is blank, uses the provided part name as the operation.
     *
     * @param fields      The queue of field names.
     * @param operations  The queue of SQL operations.
     * @param field       The StringBuilder representing the current field name.
     * @param operation   The StringBuilder representing the current SQL operation.
     * @param partName    The part name to be considered as a field or operation.
     */
    private static void addFieldAndOperation(Queue<String> fields, Queue<String> operations, StringBuilder field,
                                             StringBuilder operation, String partName) {
        fields.add(field.toString());
        field.setLength(0);

        var currentOperation = operation.toString();
        operations.add(currentOperation.isBlank() ? partName : currentOperation);
        operation.setLength(0);
    }

    /**
     * Adds any remaining fields and operations to their respective queues.
     * Checks if the field and operation StringBuilders are not empty before adding.
     *
     * @param fields      The queue of field names.
     * @param operations  The queue of SQL operations.
     * @param field       The StringBuilder representing the current field name.
     * @param operation   The StringBuilder representing the current SQL operation.
     */
    private static void addRemainingFieldsAndOperations(Queue<String> fields, Queue<String> operations,
                                                        StringBuilder field, StringBuilder operation) {
        if (!field.isEmpty()) {
            fields.add(field.toString());
        }

        if (!operation.isEmpty()) {
            operations.add(operation.toString());
        }
    }

    /**
     * Constructs a WHERE query based on the provided queues of fields and operations.
     * Converts field names to underscore format and appends corresponding SQL operations.
     * The constructed WHERE query is logged as debug information before being returned.
     *
     * @param fields     The queue of field names to be included in the WHERE query.
     * @param operations The queue of SQL operations corresponding to the fields.
     * @return The constructed WHERE query.
     */
    private static String buildWhereQuery(Queue<String> fields, Queue<String> operations) {
        var whereQuery = new StringBuilder();
        while (!fields.isEmpty()) {
            String currentField = fields.poll();
            String convertToUnderscoreField = convertToUnderscore(currentField);
            whereQuery.append(convertToUnderscoreField);

            String currentOperation = operations.poll();
            appendSqlOperation(whereQuery, currentOperation);
        }

        log.debug(WHERE + whereQuery);
        return whereQuery.toString();
    }

    /**
     * Appends the SQL operation to the given WHERE query based on the provided operation name.
     * If the operation name is null, appends a default equality operation with a placeholder parameter.
     *
     * @param whereQuery      The StringBuilder representing the WHERE query being constructed.
     * @param currentOperation The operation name to be appended to the WHERE query.
     */
    private static void appendSqlOperation(StringBuilder whereQuery, String currentOperation) {
        if (currentOperation != null) {
            String sqlOperation = OPERATION_TO_SQL_CONDITIONS.get(currentOperation);
            whereQuery.append(sqlOperation);
        } else {
            if (!whereQuery.isEmpty()) {
                whereQuery.append(EQ).append(PARAMETER);
            }
        }
    }

    /**
     * Converts a given column name to underscore format.
     *
     * @param originalColumnName The original column name.
     * @return The column name in underscore format.
     */
    private static String convertToUnderscore(String originalColumnName) {
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
     * Retrieves parameter names for a given method or constructor, considering the @Param annotation.
     *
     * @param methodOrConstructor The method or constructor for which to retrieve parameter names.
     * @return A list of parameter names.
     */
    public static List<String> getParameterNames(AccessibleObject methodOrConstructor) {
        String[] parameterNames = info.lookupParameterNames(methodOrConstructor, false);
        if (parameterNames.length == 0) {
            log.debug(WILL_RESOLVE_AS_REGULAR_METHOD_PARAMETERS, methodOrConstructor);
            return Arrays.stream(((Method) methodOrConstructor).getParameters())
                    .map(Parameter::getName)
                    .toList();
        }
        return Arrays.stream(parameterNames).toList();
    }

    /**
     * Custom implementation of {@link AnnotationParanamer} to handle the @Param annotation.
     */
    private static class ParamAnnotationParanamer extends AnnotationParanamer {

        /**
         * Constructs a new ParamAnnotationParanamer with the specified fallback paranamer.
         *
         * @param fallback The fallback paranamer.
         */
        public ParamAnnotationParanamer(Paranamer fallback) {
            super(fallback);
        }

        /**
         * Retrieves the named value from the @Param annotation.
         *
         * @param ann The annotation.
         * @return The named value if the annotation is of type @Param, otherwise null.
         */
        @Override
        protected String getNamedValue(Annotation ann) {
            return ann instanceof Param param ? param.value() : null;
        }

        /**
         * Checks if the given annotation is of type @Param.
         *
         * @param ann The annotation.
         * @return True if the annotation is of type @Param, otherwise false.
         */
        @Override
        protected boolean isNamed(Annotation ann) {
            return Objects.equals(Param.class, ann.annotationType());
        }
    }
}
