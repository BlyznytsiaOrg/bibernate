package io.github.blyznytsiaorg.bibernate.dao.method;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents metadata information for a method, including its name, return type, and parameters.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@ToString
@Getter
public class MethodMetadata {
    /**
     * The name of the method.
     */
    private final String name;
    /**
     * The return type of the method.
     */
    private final ReturnType returnType;
    /**
     * The list of parameters associated with the method.
     */
    private final List<Parameter> parameters;

    /**
     * Constructs a MethodMetadata instance with the given method name and return type.
     *
     * @param name       The name of the method.
     * @param returnType The return type of the method.
     */
    public MethodMetadata(String name, ReturnType returnType) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    /**
     * Adds a parameter to the method metadata.
     *
     * @param name The name of the parameter.
     * @param type The type of the parameter.
     */
    public void addParameter( String name, String type) {
        parameters.add(new Parameter(name, type));
    }
}
