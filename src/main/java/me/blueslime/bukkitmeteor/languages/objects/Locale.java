package me.blueslime.bukkitmeteor.languages.objects;

public class Locale {

    private final String country;
    private final String lang;

    private Locale(String lang) {
        this(lang, null);
    }

    private Locale(String lang, String country) {
        this.country = country;
        this.lang = lang;
    }

    public String getLanguage() {
        return lang;
    }

    public String getCountry() {
        return country;
    }

    public boolean hasCountry() {
        return country != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Locale) {
            return hashCode() == o.hashCode();
        }
        return false;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Locale clone() throws CloneNotSupportedException {
        return new Locale(lang, country);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (hasCountry()) {
            return lang + "_" + country;
        } else {
            return lang;
        }
    }

    public static Locale fromString(String localeString) throws InvalidLocaleException {
        int separatorIndex = localeString.indexOf('_');

        if (separatorIndex != -1) {
            final String lang = localeString.substring(0, separatorIndex).toLowerCase();
            final String country = localeString.substring(separatorIndex + 1).toUpperCase();

            if (lang.length() == 2 && country.length() == 2) {
                return new Locale(lang, country);
            } else {
                throw new InvalidLocaleException(localeString);
            }
        } else {
            // language only, no country.
            final String lang = localeString.toLowerCase();

            if (lang.length() == 2) {
                return new Locale(lang);
            } else {
                throw new InvalidLocaleException(localeString);
            }
        }
    }
}
