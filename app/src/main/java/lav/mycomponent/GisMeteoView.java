package lav.mycomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import lav.testgismeteo.R;

public class GisMeteoView extends View {

    private int[] def_colors = {0XFFFF0000, 0XFFFF7800, 0XFFFFD300, 0XFF68D910, 0XFFC8EFA7, 0XFFC8F0FF, 0XFF20C5FF, 0XFF1045FF, 0XFF6700F9, 0XFF5600AF};
    private int[] def_temper = {    40,         30,         20,         10,          1,           0,        -10,       -20,        -30,        -40    };

    public static final String tagDate = "tagDate";
    public static final String tagHour = "tagHour";
    public static final String tagTemperature = "tagTemperature";

    private GestureDetector mDetector;

    private final float BORDER_WIDTH = 3;           // ширина рамки вокруг компонента
    private final float LINE_WIDTH = 5;             // ширина линии графа
    private final float TEXT_OVER_GRAPH = 5;        // промежуток между текстом температуры и графиком
    private final float TEMPERATURE_TEXT_SIZE = 12; // размер текста температуры
    private final float DATE_TEXT_SIZE = 20;        // размер текста отображения даты
    private final float TIME_TEXT_SIZE = 12;        // размер текста отображения времени
    private final float MIN_WIDTH_ITEM = 45;        // минимальная ширина элемента графа
    private final float BOTTOM_PEDDING = 5;         // отступ снизу
    private final float BETWEEN_DATES = 10;         // расстояние между отображением дней

    private int mBackgroudColor = Color.WHITE;
    private int mBorderColor = Color.BLACK;

    private float canvaWigth;
    private float canvaHeight;
    private float canvaOffset = 0;
    private float gistogramWidth;

    private Paint lineP;
    private Paint v_lineP;
    private Paint tempTextP;
    private Paint hourTextP;
    private Paint dateTextP;
    private Paint dateTextPRight;
    private Paint fillText;

    private Paint borderPaint;

    private ArrayList<TemperatureItem> mTemperatureAdapter;

    private boolean mShowTemperature = true; // показывать температуру или нет

    private void inicialisation(Context context){

        borderPaint = new Paint();
        borderPaint.setColor(mBorderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_WIDTH);

        mDetector = new GestureDetector(context, new GismeteoViewGestureListener());

        // кисть для горизонтальной линии
        lineP = new Paint();
        lineP.setStrokeWidth(LINE_WIDTH);
        lineP.setStyle(Paint.Style.STROKE);

        // кисть для вертикальной линии
        v_lineP = new Paint();
        v_lineP.setStrokeWidth(LINE_WIDTH);
        v_lineP.setStyle(Paint.Style.STROKE);

        // кисть для текста температуры
        tempTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempTextP.setStyle(Paint.Style.STROKE);
        tempTextP.setTextSize(TEMPERATURE_TEXT_SIZE);
        tempTextP.setTextAlign(Paint.Align.CENTER);

        // кисть для текста времени
        hourTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourTextP.setStyle(Paint.Style.STROKE);
        hourTextP.setTextSize(TIME_TEXT_SIZE);
        hourTextP.setTextAlign(Paint.Align.CENTER);

        // кисть для текста даты
        dateTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextP.setStyle(Paint.Style.STROKE);
        dateTextP.setTextSize(DATE_TEXT_SIZE);
        dateTextP.setTextAlign(Paint.Align.LEFT);

        // кисть для текста с правым выравниванием
        dateTextPRight = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextPRight.setStyle(Paint.Style.STROKE);
        dateTextPRight.setTextSize(DATE_TEXT_SIZE);
        dateTextPRight.setTextAlign(Paint.Align.RIGHT);

        // кисть для закрашивания надписи в цвет фона
        fillText = new Paint();
        fillText.setStyle(Paint.Style.FILL_AND_STROKE);
        fillText.setColor(mBackgroudColor);
    }

    /**
     * показать/скрыть температуру
     * @param showTemperature
     */
    public void setShowTemperature(boolean showTemperature) {
        this.mShowTemperature = showTemperature;
        invalidate();
    }

    public GisMeteoView(Context context) {
        super(context);
        inicialisation(context);
    }

    public GisMeteoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicialisation(context);
    }

    public GisMeteoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inicialisation(context);
    }

    public void setTemperatureAdapter(ArrayList<HashMap<String, String>> adapter){
        mTemperatureAdapter = new ArrayList<TemperatureItem>();
        // формируем список элементов
        for(HashMap<String, String> item: adapter){
            mTemperatureAdapter.add(new TemperatureItem(item.get(tagHour),
                                                        item.get(tagDate),
                                                        Integer.parseInt(item.get(tagTemperature))));
        }
        // настраиваем эти элементы
        calculateItemParam();
    }

    /**
     * расчитывает цвет в зависимости от температуры
     * @return - цвет
     */
    int getTemperatureColor(int temperature){
        for (int i = 0; i < def_temper.length; i++){
            if (temperature == def_temper[i]){
                return def_colors[i];
            }
        }

        if (def_temper[0] < temperature){
            return def_colors[0];
        }

        if (def_temper[def_temper.length - 1] > temperature) {
            return def_colors[def_temper.length - 1];
        }

        int red, green, blue;
        int max_indx = -1, min_indx = -1;
        int delta_def_temp, delta_temp;
        for(int i = 1; i < def_temper.length; i++){
            if ((temperature < def_temper[i - 1]) && (temperature > def_temper[i])){
                max_indx = i - 1;
                min_indx = i;
                break;
            }
        }
        delta_def_temp = def_temper[max_indx] - def_temper[min_indx];
        delta_temp = temperature - def_temper[min_indx];
        red = Color.red(def_colors[min_indx]) + ((Color.red(def_colors[max_indx]) - Color.red(def_colors[min_indx])) / delta_def_temp) * delta_temp;
        green = Color.green(def_colors[min_indx]) + ((Color.green(def_colors[max_indx]) - Color.green(def_colors[min_indx])) / delta_def_temp) * delta_temp;
        blue = Color.blue(def_colors[min_indx]) + ((Color.blue(def_colors[max_indx]) - Color.blue(def_colors[min_indx])) / delta_def_temp) * delta_temp;

        return Color.rgb(red, green, blue);
    }

    /**
     * рассчитывает параметры элементов температуры
     */
    private void calculateItemParam(){
        if(isInEditMode()){
            return;
        }
        if (canvaHeight == 0){
            return;
        }
        TemperatureItem item;
        float maxTemp = -999, minTemp = 999, temp;
        float deltaY = 0;

        // определяем максимальную и минимальную температуру из имеющихся
        for (int i = 0; i < mTemperatureAdapter.size(); i++){
            temp = mTemperatureAdapter.get(i).temperature;
            maxTemp = maxTemp < temp ? temp : maxTemp;
            minTemp = minTemp > temp ? temp : minTemp;
        }

        // число пикселей по оси Y на 1 градус
        float infoZoneHeight = DATE_TEXT_SIZE + TIME_TEXT_SIZE + TEMPERATURE_TEXT_SIZE + BORDER_WIDTH + TEXT_OVER_GRAPH;
        float gistogram_height = canvaHeight - infoZoneHeight - (BOTTOM_PEDDING + BORDER_WIDTH);
        if (Math.abs(maxTemp - minTemp) != 0) {
            deltaY = gistogram_height / Math.abs(maxTemp - minTemp);
        }

        for (int i = 0; i < mTemperatureAdapter.size(); i++){
            item = mTemperatureAdapter.get(i);
            item.startX = i * MIN_WIDTH_ITEM;
            item.endX = item.startX + MIN_WIDTH_ITEM;
            item.Y = gistogram_height - (float)Math.ceil(deltaY * Math.abs(item.temperature - minTemp)) +
                    infoZoneHeight;
            item.showColor = getTemperatureColor(item.temperature);
        }

        gistogramWidth = mTemperatureAdapter.size() * MIN_WIDTH_ITEM + 2 * BORDER_WIDTH;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        canvaHeight = getMeasuredHeight();
        canvaWigth = getMeasuredWidth();
        calculateItemParam();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInEditMode()){
            // закрашиваем фон
            canvas.drawColor(mBackgroudColor);

            TemperatureItem item = null, prev_item = null, prevDrawDate= null;
            int firstIndex = 0;
            // определяем первый видимый элемент
            for (int i = 0; i < mTemperatureAdapter.size(); i++){
                if (mTemperatureAdapter.get(i).endX > canvaOffset){
                    firstIndex = i;
                    break;
                }
            }

            // сдвигаем канву на необходимое расстояние
            canvas.translate(-canvaOffset, 0);

            for (int i = firstIndex; (i < mTemperatureAdapter.size()) && (mTemperatureAdapter.get(i).startX < (canvaWigth + canvaOffset)); i++){
                item = mTemperatureAdapter.get(i);
                lineP.setColor(item.showColor);
                canvas.drawLine(item.startX, item.Y, item.endX, item.Y, lineP);

                if (i > firstIndex){
                    prev_item = mTemperatureAdapter.get(i - 1);
                    if (prev_item.temperature > item.temperature) {
                        v_lineP.setShader(new LinearGradient(item.startX,
                                                            prev_item.Y - LINE_WIDTH / 2.0f,
                                                            item.startX,
                                                            item.Y + LINE_WIDTH / 2.0f,
                                                            prev_item.showColor,
                                                            item.showColor,
                                                            Shader.TileMode.CLAMP));
                        canvas.drawLine(item.startX, prev_item.Y - LINE_WIDTH / 2.0f, item.startX, item.Y + LINE_WIDTH / 2.0f, v_lineP);
                    }
                    else {
                        v_lineP.setShader(new LinearGradient(item.startX,
                                                            prev_item.Y + LINE_WIDTH / 2.0f,
                                                            item.startX,
                                                            item.Y - LINE_WIDTH / 2.0f,
                                                            prev_item.showColor,
                                                            item.showColor,
                                                            Shader.TileMode.CLAMP));
                        canvas.drawLine(item.startX, prev_item.Y + LINE_WIDTH / 2.0f, item.startX, item.Y - LINE_WIDTH / 2.0f, v_lineP);
                    }
                }

                // выводим время
                canvas.drawText(item.hour,
                                item.startX + (item.endX - item.startX) / 2,
                                BORDER_WIDTH + DATE_TEXT_SIZE + TIME_TEXT_SIZE,
                                hourTextP);

                // выводим дату
                if ((prev_item == null) || (item.day.compareToIgnoreCase(prev_item.day)) != 0){
                    float begin_x = prevDrawDate == null ? canvaOffset + BORDER_WIDTH : item.startX + BORDER_WIDTH;
                    if (prevDrawDate != null){
                        float prevTextWidth = dateTextP.measureText(prevDrawDate.day);
                        if ((canvaOffset + BORDER_WIDTH + prevTextWidth >= begin_x - BETWEEN_DATES)){
                            // закрашиваем предыдущий текст
                            canvas.drawRect(BORDER_WIDTH,
                                            BORDER_WIDTH,
                                            canvaOffset + BORDER_WIDTH + prevTextWidth + BETWEEN_DATES,
                                            BORDER_WIDTH + DATE_TEXT_SIZE,
                                            fillText);
                            // рисуем дату
                            canvas.drawText(prevDrawDate.day,
                                            begin_x - BETWEEN_DATES,
                                            BORDER_WIDTH + DATE_TEXT_SIZE,
                                            dateTextPRight);
                        }
                    }
                    canvas.drawText(item.day,
                                    begin_x,
                                    BORDER_WIDTH + DATE_TEXT_SIZE,
                                    dateTextP);
                    prevDrawDate = item;
                }

                // выводим температуру если нужно
                if(mShowTemperature){
                    String temp_str = String.valueOf(item.temperature);
                    if (item.temperature > 0) temp_str = "+" + temp_str;
                    if (item.temperature < 0) temp_str = "-" + temp_str;

                    canvas.drawText(temp_str,
                            item.startX + (item.endX - item.startX) / 2,
                            item.Y - TEXT_OVER_GRAPH,
                            tempTextP);
                }
            }

            // рамка
            canvas.drawRect(BORDER_WIDTH / 2 + canvaOffset, BORDER_WIDTH / 2, canvaWigth - BORDER_WIDTH / 2 + canvaOffset, canvaHeight - BORDER_WIDTH / 2, borderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);

        return true;
    }


    private class GismeteoViewGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            canvaOffset += distanceX;
            if(canvaOffset < 0) {
                canvaOffset = 0;
            }
            if(canvaOffset > gistogramWidth - canvaWigth){
                canvaOffset = gistogramWidth - canvaWigth;
            }
            invalidate();
            return true;
        }
    }

}
