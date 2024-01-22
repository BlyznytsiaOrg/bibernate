package io.github.blyznytsiaorg.bibernate.dao.method;

import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;

@ToString
@Getter
public class ReturnType {

    private ParameterizedType genericEntityClass;

    private Class<?> entityClass;

    public ReturnType(ParameterizedType entityClass) {
        this.genericEntityClass = entityClass;
    }

    public ReturnType(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
}