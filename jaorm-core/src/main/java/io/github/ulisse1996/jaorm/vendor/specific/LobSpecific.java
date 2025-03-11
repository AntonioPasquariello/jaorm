package io.github.ulisse1996.jaorm.vendor.specific;

public interface LobSpecific extends Specific {

    String convertSubStringSupport(String alias, String columnName, int maxLength);
}
