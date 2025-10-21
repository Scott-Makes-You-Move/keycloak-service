package models;

import java.util.TimeZone;

public record GeoInformation(String ip, String network, String version, String city, String region,
                             String region_code, String country, String country_name, String country_code,
                             String country_code_iso3, String country_capital, String country_tld,
                             String continent_code, String in_eu, String postal, float latitude, float longitude,
                             TimeZone timezone, String utc_offset, String country_calling_code, String currency,
                             String currency_name, String languages, float country_area, int country_population,
                             String asn, String org) {

}
