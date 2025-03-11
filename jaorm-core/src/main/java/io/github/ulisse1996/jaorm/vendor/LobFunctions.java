package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.specific.LobSpecific;

import java.util.Objects;

public class LobFunctions {

    private LobFunctions() { throw new UnsupportedOperationException("No instance for utility class !"); }

    private static final int MAX_STRING_VALUE = 32000;

    public static VendorFunction<String> substr(SqlColumn<?, String> column, int maxLength) {
        return new LobSubstr(column, maxLength);
    }

    public static VendorFunction<String> substr(SqlColumn<?, String> column) {

        return new LobSubstr(column, MAX_STRING_VALUE);
    }


    private static class LobSubstr implements VendorFunction<String> {

        private final SqlColumn<?, String> column;
        private final int maxLength;

        public LobSubstr(SqlColumn<?, String> column, int maxLength) {
            Objects.requireNonNull(column, "Column can't be null !");
            this.column = column;
            this.maxLength = maxLength;
        }

        @Override
        public String apply(String alias) {
            LobSpecific specific = VendorSpecific.getSpecific(LobSpecific.class);
            return specific.convertSubStringSupport(alias, column.getName(), this.maxLength);
        }

        @Override
        public boolean isString() {
            return true;
        }
    }
    
}
