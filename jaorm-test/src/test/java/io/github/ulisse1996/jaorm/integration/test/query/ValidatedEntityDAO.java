package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.entity.ValidatedEntity;

@Dao
public interface ValidatedEntityDAO extends BaseDao<ValidatedEntity> {
}
