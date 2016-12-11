package com.gbbtbb.homehub.agendaviewer;

public class WeatherItem {

    private long datetime;
    private int weatherId;
    private double weather_temperature;
    private int weather_humidity;
    private long sunrise;
    private long sunset;

    public WeatherItem(long datetime, int wheatherId, int weather_humidity, double weather_temperature, long sunrise, long sunset) {
        this.datetime = datetime;
        this.weatherId = wheatherId;
        this.weather_temperature = weather_temperature;
        this.weather_humidity = weather_humidity;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public long getDatetime() { return datetime; }
    public void setDatetime(long datetime) { this.datetime = datetime; }

    public int getWeatherId() { return weatherId; }
    public void setWeatherId(int id) { this.weatherId = id; }

    public int getWeatherHumidity() { return weather_humidity; }
    public void setWeatherHumidity(int weather_humidity) { this.weather_humidity = weather_humidity; }

    public double getWeatherTemperature() { return weather_temperature; }
    public void setWeatherTemperature(double weather_temperature) { this.weather_temperature = weather_temperature; }

    public long getSunrise() { return sunrise; }
    public void setSunrise(long sunrise) { this.sunrise = sunrise; }

    public long getSunset() { return sunset; }
    public void setSunset(long sunset) { this.sunset = sunset; }
}
