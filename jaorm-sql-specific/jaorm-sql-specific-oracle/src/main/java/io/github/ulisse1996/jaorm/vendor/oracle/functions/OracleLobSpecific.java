package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.vendor.specific.LobSpecific;

public class OracleLobSpecific implements LobSpecific {


    @Override
    public String convertSubStringSupport(String alias, String columnName, int maxLength) {
        return String.format("DBMS_LOB.SUBSTR(%s.%s, %d, %d)", alias, columnName, maxLength, 1);
    }
}
