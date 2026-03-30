public enum WeatherType {

    SUNNY,
    CLEAR,
    STORM;

    public boolean isBadWeather() {
        return this == STORM;
    }
}