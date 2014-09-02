package lav.mycomponent;

import android.graphics.Color;

public class TemperatureItem {

    float startX;
    float endX;

    float Y;
    float second_Y;

    String hour;
    String day;
    int temperature;
    int second_temperature;

    int showColor;
    int second_showColor;

    public TemperatureItem(float startX, float endX, float Y, float second_Y,
                           String hour, String day, int temperature, int second_temperature,
                           int showColor){
        this.startX = startX;
        this.endX = endX;
        this.Y = Y;
        this.second_Y = second_Y;
        this.hour = hour;
        this.day = day;
        this.temperature = temperature;
        this.second_temperature = second_temperature;
        this.showColor = showColor;
        this.second_showColor = Color.GREEN;
    }

    public TemperatureItem(String hour, String day, int temperature, int second_temperature){
        this.startX = 0;
        this.endX = 0;
        this.Y = 0;
        this.second_Y = 0;
        this.hour = hour;
        this.day = day;
        this.temperature = temperature;
        this.second_temperature = second_temperature;
        this.showColor = Color.GREEN;
        this.second_showColor = Color.GREEN;
    }
}
