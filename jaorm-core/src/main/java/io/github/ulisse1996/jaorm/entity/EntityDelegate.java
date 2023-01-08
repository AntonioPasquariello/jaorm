package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.entity.relationship.LazyEntityInfo;
import io.github.ulisse1996.jaorm.entity.relationship.RelationshipManager;
import io.github.ulisse1996.jaorm.schema.TableInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;

public interface EntityDelegate<T> {

    @SuppressWarnings("unchecked")
    static <R> R unboxEntity(R e) {
        if (e instanceof EntityDelegate<?>) {
            return (R) ((EntityDelegate<?>) e).getEntity();
        }
        return e;
    }

    EntityDelegate<T> generateDelegate();
    Supplier<T> getEntityInstance();
    EntityMapper<T> getEntityMapper();
    void setEntity(ResultSet rs) throws SQLException;
    void setFullEntity(T entity);
    void setFullEntityFullColumns(Map<SqlColumn<T, ?>, ?> columns);
    T getEntity();
    String getBaseSql();
    String getKeysWhere();
    String getKeysWhere(String alias);
    String getInsertSql();
    String[] getSelectables();
    String getTable();
    String getUpdateSql();
    String getDeleteSql();
    boolean isModified();
    boolean isDefaultGeneration();
    T initDefault(T entity);
    TableInfo toTableInfo();
    DirtinessTracker getTracker();
    boolean isLazyEntity();
    LazyEntityInfo getLazyInfo();
    void setLazyInfo(LazyEntityInfo info);
    RelationshipManager<T> getRelationshipManager();
    default void setAutoGenerated(Map<String, Object> generated) {
        if (!generated.isEmpty()) {
            EntityMapper<T> mapper = getEntityMapper();
            T entity = getEntity();
            for (Map.Entry<String, Object> entry : generated.entrySet()) {
                mapper.setGenerated(entity, entry.getKey(), entry.getValue());
            }
        }
    }
    default Map<String, Class<?>> getAutoGenerated() {
        return getEntityMapper().getAutoGenerated();
    }
    default T toEntity(ResultSet rs) throws SQLException {
        return getEntityMapper().map(getEntityInstance(), rs);
    }
}
