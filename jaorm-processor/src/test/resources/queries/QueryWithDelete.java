package io.test;

import io.jaorm.processor.annotation.Query;

public interface QueryWithDelete {

    @Query(sql = "DELETE I WHERE E = ? AND ER = ?")
    void delete(String e, String er);
}