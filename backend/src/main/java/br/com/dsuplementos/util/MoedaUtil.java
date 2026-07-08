package br.com.dsuplementos.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoedaUtil {
    private static final Locale PT_BR = new Locale.Builder()
            .setLanguage("pt")
            .setRegion("BR")
            .build();

    private MoedaUtil() {
    }

    public static String formatar(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }
}
