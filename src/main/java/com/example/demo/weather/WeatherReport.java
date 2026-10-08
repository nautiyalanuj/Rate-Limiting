//Copyright ANuj Nautiyal
package com.example.demo.weather;

/** Public output stays stable even if the provider adds or renames fields. */
public record WeatherReport(String city, String country, double temperatureCelsius, String condition) { }
