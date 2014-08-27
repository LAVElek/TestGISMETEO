package lav.mycomponent;

import android.graphics.Color;

public class TemperatureItem {

    float startX;
    float endX;

    float Y;

    String hour;
    String day;
    int temperature;

    int showColor;

    public TemperatureItem(float startX, float endX, float Y,
                           String hour, String day, int temperature,
                           int showColor){
        this.startX = startX;
        this.endX = endX;
        this.Y = Y;
        this.hour = hour;
        this.day = day;
        this.temperature = temperature;
        this.showColor = showColor;
    }

    public TemperatureItem(String hour, String day, int temperature){
        this.startX = 0;
        this.endX = 0;
        this.Y = 0;
        this.hour = hour;
        this.day = day;
        this.temperature = temperature;
        this.showColor = Color.GREEN;
    }
}
