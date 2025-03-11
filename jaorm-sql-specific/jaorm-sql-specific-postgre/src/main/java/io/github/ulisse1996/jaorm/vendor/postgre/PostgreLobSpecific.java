package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.LobSpecific;

public class PostgreLobSpecific implements LobSpecific {

    @Override
    public String convertSubStringSupport(String alias, String columnName, int maxLength) {
        return String.format("SUBSTR(%s.%s, %d, %d)", alias, columnName, 0, maxLength+1);
    }
}
