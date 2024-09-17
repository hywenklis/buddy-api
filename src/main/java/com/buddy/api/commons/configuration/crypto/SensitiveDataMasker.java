package com.buddy.api.commons.configuration.crypto;


import java.util.regex.Pattern;

public class SensitiveDataMasker {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");
    private static final Pattern FORMATTED_CPF_PATTERN = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@.*$");

    public String mask(Object obj) {
        if (obj == null) {
            return "null";
        }
        final String str = obj.toString();

        if (isCpf(str)) {
            return maskCpf(str);
        } else if (isEmail(str)) {
            return maskEmail(str);
        }

        return str;
    }

    private boolean isCpf(String str) {
        return CPF_PATTERN.matcher(str).matches() || FORMATTED_CPF_PATTERN.matcher(str).matches();
    }

    private String maskCpf(String cpf) {
        if (CPF_PATTERN.matcher(cpf).matches()) {
            return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.***.***-$4");
        } else if (FORMATTED_CPF_PATTERN.matcher(cpf).matches()) {
            return cpf.replaceAll("(\\d{3})\\.\\d{3}\\.\\d{3}-(\\d{2})", "$1.***.***-$2");
        }
        return cpf;
    }

    private boolean isEmail(String str) {
        return EMAIL_PATTERN.matcher(str).matches();
    }

    private String maskEmail(String email) {
        return email.replaceAll("(^[^@]+)(@.*)", "****$2");
    }
}
