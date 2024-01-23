package io.github.blyznytsiaorg.bibernate.dao.method;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@ToString
@Getter
public class MethodMetadata {
    private final String name;
    private final ReturnType returnType;
    private final List<Parameter> parameters;

    public MethodMetadata(String name, ReturnType returnType) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    public void addParameter( String name, String type) {
        parameters.add(new Parameter(name, type));
    }
}
