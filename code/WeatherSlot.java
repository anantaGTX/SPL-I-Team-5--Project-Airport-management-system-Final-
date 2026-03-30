import java.time.LocalDateTime;

public class WeatherSlot {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WeatherType type;

    public WeatherSlot(LocalDateTime startTime, LocalDateTime endTime, WeatherType type) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    public boolean contains(LocalDateTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public WeatherType getType() {
        return type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}