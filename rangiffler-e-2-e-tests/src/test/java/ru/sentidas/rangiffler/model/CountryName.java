package ru.sentidas.rangiffler.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public enum CountryName {
    TOGO("Togo"),
    LAO_PEOPLES_DEMOCRATIC_REPUBLIC("Lao People`s Democratic Republic"),
    MAURITANIA("Mauritania"),
    NICARAGUA("Nicaragua"),
    LATVIA("Latvia"),
    OMAN("Oman"),
    AFGHANISTAN("Afghanistan"),
    CYPRUS("Cyprus"),
    BENIN("Benin"),
    ANTARCTICA("Antarctica"),
    CHINA("China"),
    COLOMBIA("Colombia"),
    CHRISTMAS_ISLAND("Christmas Island"),
    ANTIGUA_AND_BARBUDA("Antigua and Barbuda"),
    MONTSERRAT("Montserrat"),
    MOLDOVA_REPUBLIC_OF("\"Moldova, Republic of\""),
    ZAMBIA("Zambia"),
    VIET_NAM("Viet Nam"),
    FRENCH_SOUTHERN_TERRITORIES("French Southern Territories"),
    CHAD("Chad"),
    MAYOTTE("Mayotte"),
    LEBANON("Lebanon"),
    LUXEMBOURG("Luxembourg"),
    MARTINIQUE("Martinique"),
    CZECH_REPUBLIC("Czech Republic"),
    UNITED_ARAB_EMIRATES("United Arab Emirates"),
    CAMEROON("Cameroon"),
    BURUNDI("Burundi"),
    ARGENTINA("Argentina"),
    AMERICAN_SAMOA("American Samoa"),
    BAHRAIN("Bahrain"),
    CHILE("Chile"),
    ANDORRA("Andorra"),
    NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands"),
    LITHUANIA("Lithuania"),
    MADAGASCAR("Madagascar"),
    SAINT_LUCIA("Saint Lucia"),
    TURKEY("Turkey"),
    UKRAINE("Ukraine"),
    TUVALU("Tuvalu"),
    VIRGIN_ISLANDS_US("\"Virgin Islands, U.S.\""),
    MALTA("Malta"),
    NORWAY("Norway"),
    MONACO("Monaco"),
    SWITZERLAND("Switzerland"),
    ARUBA("Aruba"),
    BELIZE("Belize"),
    BERMUDA("Bermuda"),
    COTE_D_IVOIRE("Cote D`Ivoire"),
    MAURITIUS("Mauritius"),
    UNITED_STATES("United States"),
    TAIWAN_PROVINCE_OF_CHINA("\"Taiwan, Province of China\""),
    YEMEN("Yemen"),
    MALAWI("Malawi"),
    NETHERLANDS("Netherlands"),
    LESOTHO("Lesotho"),
    BOLIVIA("Bolivia"),
    AUSTRIA("Austria"),
    COOK_ISLANDS("Cook Islands"),
    BELARUS("Belarus"),
    AUSTRALIA("Australia"),
    BRUNEI_DARUSSALAM("Brunei Darussalam"),
    MOROCCO("Morocco"),
    NEW_ZEALAND("New Zealand"),
    LIBERIA("Liberia"),
    MALDIVES("Maldives"),
    TURKS_AND_CAICOS_ISLANDS("Turks and Caicos Islands"),
    UGANDA("Uganda"),
    TRINIDAD_AND_TOBAGO("Trinidad and Tobago"),
    POLAND("Poland"),
    INDIA("India"),
    GEORGIA("Georgia"),
    GREECE("Greece"),
    SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS("South Georgia and the South Sandwich Islands"),
    GRENADA("Grenada"),
    BRITISH_INDIAN_OCEAN_TERRITORY("British Indian Ocean Territory"),
    HONG_KONG("Hong Kong"),
    KOREA_DEMOCRATIC_PEOPLES_REPUBLIC_OF("\"Korea, Democratic People`s Republic of\""),
    KYRGYZSTAN("Kyrgyzstan"),
    SAINT_PIERRE_AND_MIQUELON("Saint Pierre and Miquelon"),
    EL_SALVADOR("El Salvador"),
    REUNION("Reunion"),
    SAUDI_ARABIA("Saudi Arabia"),
    SEYCHELLES("Seychelles"),
    SAO_TOME_AND_PRINCIPE("Sao Tome and Principe"),
    KENYA("Kenya"),
    KOREA_REPUBLIC_OF("\"Korea, Republic of\""),
    FRENCH_GUIANA("French Guiana"),
    DJIBOUTI("Djibouti"),
    EQUATORIAL_GUINEA("Equatorial Guinea"),
    GUADELOUPE("Guadeloupe"),
    DENMARK("Denmark"),
    ISRAEL("Israel"),
    PITCAIRN("Pitcairn"),
    SOLOMON_ISLANDS("Solomon Islands"),
    PARAGUAY("Paraguay"),
    RUSSIAN_FEDERATION("Russian Federation"),
    KUWAIT("Kuwait"),
    DOMINICAN_REPUBLIC("Dominican Republic"),
    GUATEMALA("Guatemala"),
    UNITED_KINGDOM("United Kingdom"),
    GUAM("Guam"),
    HEARD_ISLAND_AND_MCDONALD_ISLANDS("Heard Island and Mcdonald Islands"),
    SINGAPORE("Singapore"),
    PAKISTAN("Pakistan"),
    SURINAME("Suriname"),
    SWEDEN("Sweden"),
    JAPAN("Japan"),
    GUINEA_BISSAU("Guinea-Bissau"),
    WESTERN_SAHARA("Western Sahara"),
    ALGERIA("Algeria"),
    GABON("Gabon"),
    FRANCE("France"),
    DOMINICA("Dominica"),
    HONDURAS("Honduras"),
    SUDAN("Sudan"),
    RWANDA("Rwanda"),
    PHILIPPINES("Philippines"),
    QATAR("Qatar"),
    PERU("Peru"),
    PUERTO_RICO("Puerto Rico"),
    SLOVENIA("Slovenia"),
    HAITI("Haiti"),
    SPAIN("Spain"),
    GREENLAND("Greenland"),
    GAMBIA("Gambia"),
    ERITREA("Eritrea"),
    FINLAND("Finland"),
    ESTONIA("Estonia"),
    SAINT_KITTS_AND_NEVIS("Saint Kitts and Nevis"),
    HUNGARY("Hungary"),
    IRAQ("Iraq"),
    CAYMAN_ISLANDS("Cayman Islands"),
    SAINT_HELENA("Saint Helena"),
    PALESTINIAN_TERRITORY_OCCUPIED("\"Palestinian Territory, Occupied\""),
    FRENCH_POLYNESIA("French Polynesia"),
    SVALBARD_AND_JAN_MAYEN("Svalbard and Jan Mayen"),
    INDONESIA("Indonesia"),
    ICELAND("Iceland"),
    EGYPT("Egypt"),
    FALKLAND_ISLANDS_MALVINAS("Falkland Islands (Malvinas)"),
    FIJI("Fiji"),
    GUINEA("Guinea"),
    GUYANA("Guyana"),
    IRAN_ISLAMIC_REPUBLIC_OF("\"Iran, Islamic Republic of\""),
    COMOROS("Comoros"),
    IRELAND("Ireland"),
    KAZAKHSTAN("Kazakhstan"),
    ROMANIA("Romania"),
    SLOVAKIA("Slovakia"),
    PAPUA_NEW_GUINEA("Papua New Guinea"),
    PORTUGAL("Portugal"),
    SOMALIA("Somalia"),
    CROATIA("Croatia"),
    KIRIBATI("Kiribati"),
    JAMAICA("Jamaica"),
    ECUADOR("Ecuador"),
    ETHIOPIA("Ethiopia"),
    FAROE_ISLANDS("Faroe Islands"),
    CAMBODIA("Cambodia"),
    SYRIAN_ARAB_REPUBLIC("Syrian Arab Republic"),
    SENEGAL("Senegal"),
    PALAU("Palau"),
    SIERRA_LEONE("Sierra Leone"),
    MICRONESIA_FEDERATED_STATES_OF("\"Micronesia, Federated States of\""),
    GIBRALTAR("Gibraltar"),
    GERMANY("Germany"),
    GHANA("Ghana"),
    JORDAN("Jordan"),
    ITALY("Italy"),
    PANAMA("Panama"),
    SWAZILAND("Swaziland"),
    SAN_MARINO("San Marino"),
    TUNISIA("Tunisia"),
    MALI("Mali"),
    CONGO("Congo"),
    ANGOLA("Angola"),
    BHUTAN("Bhutan"),
    NETHERLANDS_ANTILLES("Netherlands Antilles"),
    BARBADOS("Barbados"),
    CENTRAL_AFRICAN_REPUBLIC("Central African Republic"),
    MYANMAR("Myanmar"),
    LIECHTENSTEIN("Liechtenstein"),
    NAMIBIA("Namibia"),
    MOZAMBIQUE("Mozambique"),
    TONGA("Tonga"),
    VIRGIN_ISLANDS_BRITISH("\"Virgin Islands, British\""),
    VENEZUELA("Venezuela"),
    TANZANIA_UNITED_REPUBLIC_OF("\"Tanzania, United Republic of\""),
    TURKMENISTAN("Turkmenistan"),
    MEXICO("Mexico"),
    NEW_CALEDONIA("New Caledonia"),
    MACAO("Macao"),
    SRI_LANKA("Sri Lanka"),
    CONGO_THE_DEMOCRATIC_REPUBLIC_OF_THE("\"Congo, the Democratic Republic of the\""),
    ALBANIA("Albania"),
    BOTSWANA("Botswana"),
    COSTA_RICA("Costa Rica"),
    BOUVET_ISLAND("Bouvet Island"),
    ARMENIA("Armenia"),
    AZERBAIJAN("Azerbaijan"),
    BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina"),
    MONGOLIA("Mongolia"),
    NIUE("Niue"),
    MALAYSIA("Malaysia"),
    TIMOR_LESTE("Timor-Leste"),
    SAMOA("Samoa"),
    THAILAND("Thailand"),
    NORFOLK_ISLAND("Norfolk Island"),
    LIBYAN_ARAB_JAMAHIRIYA("Libyan Arab Jamahiriya"),
    ANGUILLA("Anguilla"),
    BRAZIL("Brazil"),
    CAPE_VERDE("Cape Verde"),
    BELGIUM("Belgium"),
    CANADA("Canada"),
    BANGLADESH("Bangladesh"),
    BAHAMAS("Bahamas"),
    NIGERIA("Nigeria"),
    MACEDONIA_THE_FORMER_YUGOSLAV_REPUBLIC_OF("\"Macedonia, the Former Yugoslav Republic of\""),
    NEPAL("Nepal"),
    HOLY_SEE_VATICAN_CITY_STATE("Holy See (Vatican City State)"),
    UZBEKISTAN("Uzbekistan"),
    UNITED_STATES_MINOR_OUTLYING_ISLANDS("United States Minor Outlying Islands"),
    TOKELAU("Tokelau"),
    SAINT_VINCENT_AND_THE_GRENADINES("Saint Vincent and the Grenadines"),
    ZIMBABWE("Zimbabwe"),
    NAURU("Nauru"),
    NIGER("Niger"),
    CUBA("Cuba"),
    BURKINA_FASO("Burkina Faso"),
    BULGARIA("Bulgaria"),
    COCOS_KEELING_ISLANDS("Cocos (Keeling) Islands"),
    MARSHALL_ISLANDS("Marshall Islands"),
    SOUTH_AFRICA("South Africa"),
    URUGUAY("Uruguay"),
    WALLIS_AND_FUTUNA("Wallis and Futuna"),
    VANUATU("Vanuatu"),
    TAJIKISTAN("Tajikistan");

    private final String label;

    CountryName(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    // ---------- Поиск по любому написанию ----------
    private static final Map<String, CountryName> BY_LABEL = new HashMap<>();

    static {
        for (CountryName c : values()) {
            // по "оригинальной" строке
            BY_LABEL.put(normalize(c.label), c);
            // и по имени константы (с подчёркиваниями/без)
            BY_LABEL.put(normalize(c.name().replace('_', ' ')), c);
        }
    }

    /**
     * Нормализация строки: lower-case, убрать диакритику, кавычки/скобки/знаки,
     * свести повторные пробелы.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", ""); // убрать диакритику
        n = n.replace('`', '\'');          // backtick -> апостроф
        n = n.replace('’', '\'');          // типографский апостроф
        n = n.replaceAll("[\"()]", " ");   // кавычки/скобки -> пробел
        n = n.toLowerCase(Locale.ROOT).trim();
        n = n.replaceAll("[^\\p{L}\\p{Nd}]+", " "); // всё, кроме букв/цифр -> пробел
        n = n.replaceAll("\\s+", " ");
        return n;
    }

    /** Жёсткий разбор: бросит IAE, если не найдено. */
    public static CountryName fromLabel(String anySpelling) {
        CountryName c = BY_LABEL.get(normalize(anySpelling));
        if (c == null) {
            throw new IllegalArgumentException("Unknown country: " + anySpelling);
        }
        return c;
    }

    /** Мягкий разбор: Optional.empty(), если не найдено. */
    public static Optional<CountryName> tryParse(String anySpelling) {
        return Optional.ofNullable(BY_LABEL.get(normalize(anySpelling)));
    }
}

