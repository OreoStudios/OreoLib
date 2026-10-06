package com.oreo.lib.db;

import com.oreo.lib.OreoException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Executes repository-interface methods based on their query annotations. */
final class RepositoryHandler implements InvocationHandler {
    private static final Pattern NAMED = Pattern.compile(":(\\w+)");

    private final Db db;
    private final Class<?> repositoryInterface;
    private final Repository<Object> crud; // null unless the interface extends CrudRepository

    @SuppressWarnings("unchecked")
    RepositoryHandler(Db db, Class<?> repositoryInterface) {
        this.db = db;
        this.repositoryInterface = repositoryInterface;
        Class<?> entityType = findEntityType(repositoryInterface);
        this.crud = entityType == null ? null : (Repository<Object>) new Repository<>(db, entityType);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] rawArgs) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> "OreoRepository(" + repositoryInterface.getName() + ")";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (rawArgs == null ? null : rawArgs[0]);
                default -> null;
            };
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, rawArgs);
        }

        Object[] args = rawArgs == null ? new Object[0] : rawArgs;

        if (method.getDeclaringClass() == CrudRepository.class) {
            return crudCall(method, args);
        }

        Procedure procedure = method.getAnnotation(Procedure.class);
        if (procedure != null) {
            return callProcedure(method, procedure.value(), args);
        }

        Bound bound = bind(resolveSql(method), method, args);

        if (method.isAnnotationPresent(Modifying.class)) {
            int affected = db.sql(bound.sql).params(bound.params).run();
            return count(method.getReturnType(), affected);
        }

        List<Row> rows = db.sql(bound.sql).params(bound.params).query();
        return convert(method, rows);
    }

    private Object crudCall(Method method, Object[] args) {
        if (crud == null) {
            throw new OreoException(repositoryInterface.getName()
                + " extends CrudRepository but its entity type could not be resolved");
        }
        return switch (method.getName()) {
            case "createTable" -> { crud.createTable(); yield null; }
            case "save" -> crud.save(args[0]);
            case "findById" -> crud.findById(args[0]);
            case "existsById" -> crud.existsById(args[0]);
            case "findAll" -> crud.findAll();
            case "count" -> crud.count();
            case "deleteById" -> { crud.deleteById(args[0]); yield null; }
            case "delete" -> { crud.delete(args[0]); yield null; }
            default -> throw new OreoException("Unsupported CrudRepository method: " + method.getName());
        };
    }

    private static Class<?> findEntityType(Class<?> iface) {
        for (Type type : iface.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterized) {
                if (parameterized.getRawType() == CrudRepository.class
                    && parameterized.getActualTypeArguments()[0] instanceof Class<?> entity) {
                    return entity;
                }
                if (parameterized.getRawType() instanceof Class<?> raw) {
                    Class<?> found = findEntityType(raw);
                    if (found != null) return found;
                }
            } else if (type instanceof Class<?> raw) {
                Class<?> found = findEntityType(raw);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String resolveSql(Method method) {
        Query query = method.getAnnotation(Query.class);
        if (query != null) return query.value();
        NativeQuery nativeQuery = method.getAnnotation(NativeQuery.class);
        if (nativeQuery != null) return nativeQuery.value();
        throw new OreoException("Method " + method.getName()
            + " needs @Query, @NativeQuery or @Procedure (derived query names are not supported)");
    }

    private Bound bind(String sql, Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Map<String, Object> named = new HashMap<>();
        boolean hasNamed = false;
        for (int i = 0; i < parameters.length; i++) {
            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                named.put(param.value(), args[i]);
                hasNamed = true;
            }
        }
        if (!hasNamed) {
            return new Bound(sql, args);
        }
        Matcher matcher = NAMED.matcher(sql);
        StringBuilder rewritten = new StringBuilder();
        List<Object> ordered = new ArrayList<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!named.containsKey(name)) {
                throw new OreoException("No @Param named '" + name + "' for query: " + sql);
            }
            ordered.add(named.get(name));
            matcher.appendReplacement(rewritten, "?");
        }
        matcher.appendTail(rewritten);
        return new Bound(rewritten.toString(), ordered.toArray());
    }

    private Object callProcedure(Method method, String name, Object[] args) throws Exception {
        String placeholders = String.join(", ", Collections.nCopies(args.length, "?"));
        String call = "{call " + name + "(" + placeholders + ")}";
        try (CallableStatement statement = db.connection().prepareCall(call)) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            boolean hasResultSet = statement.execute();
            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    return convert(method, Rows.read(resultSet));
                }
            }
            return count(method.getReturnType(), statement.getUpdateCount());
        }
    }

    private Object convert(Method method, List<Row> rows) {
        Class<?> raw = method.getReturnType();
        if (raw == List.class) {
            Class<?> element = elementType(method.getGenericReturnType());
            List<Object> out = new ArrayList<>(rows.size());
            for (Row row : rows) out.add(mapRow(row, element));
            return out;
        }
        if (raw == Optional.class) {
            Class<?> element = elementType(method.getGenericReturnType());
            return rows.isEmpty() ? Optional.empty() : Optional.of(mapRow(rows.get(0), element));
        }
        if (rows.isEmpty()) {
            return raw.isPrimitive() ? EntityInfo.coerce(0, raw) : null;
        }
        return mapRow(rows.get(0), raw);
    }

    private Object mapRow(Row row, Class<?> type) {
        if (type == Row.class) return row;
        if (isScalar(type)) {
            Map<String, Object> values = row.asMap();
            Object first = values.isEmpty() ? null : values.values().iterator().next();
            return EntityInfo.coerce(first, type);
        }
        return EntityInfo.of(type).map(row);
    }

    private static boolean isScalar(Class<?> type) {
        return type == String.class || type.isPrimitive()
            || type == Integer.class || type == Long.class || type == Double.class
            || type == Float.class || type == Boolean.class;
    }

    private static Class<?> elementType(Type generic) {
        if (generic instanceof ParameterizedType parameterized
            && parameterized.getActualTypeArguments()[0] instanceof Class<?> element) {
            return element;
        }
        throw new OreoException("Cannot determine element type of " + generic
            + " (use a concrete type like List<PlayerRow>)");
    }

    private static Object count(Class<?> returnType, int affected) {
        if (returnType == void.class || returnType == Void.class) return null;
        if (returnType == long.class || returnType == Long.class) return (long) affected;
        if (returnType == boolean.class || returnType == Boolean.class) return affected > 0;
        return affected; // int / Integer
    }

    private record Bound(String sql, Object[] params) {}
}
