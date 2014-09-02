package lav.mycomponent;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import lav.testgismeteo.R;

public class GisMeteoView extends View {

    private int[] def_colors = {0XFFFF0000, 0XFFFF7800, 0XFFFFD300, 0XFF68D910, 0XFFC8EFA7, 0XFFC8F0FF, 0XFF20C5FF, 0XFF1045FF, 0XFF6700F9, 0XFF5600AF};
    private int[] def_temper = {    40,         30,         20,         10,          1,           0,        -10,       -20,        -30,        -40    };

    public static final String tagDate = "tagDate";
    public static final String tagHour = "tagHour";
    public static final String tagTemperature = "tagTemperature";

    private GestureDetector mDetector;
    private Scroller mScroller;

    private final float SPACE_UNDER_DATE_TEXT = 10;        // размер пространства после текста с датой
    private final float SPACE_UNDER_TEMPERATURE_TEXT = 10;  // промежуток между текстом температуры и графиком
    private final float SPACE_UNDER_TIME_TEXT = 10;        // промежуток после текста времени

    private final float TEMPERATURE_TEXT_SIZE = 40; // размер текста температуры
    private final float DATE_TEXT_SIZE = 30;        // размер текста отображения даты
    private final float TIME_TEXT_SIZE = 20;        // размер текста отображения времени

    private final float BORDER_WIDTH = 3;           // ширина рамки вокруг компонента
    private final float LINE_WIDTH = 5;             // ширина линии графа
    private final float MIN_WIDTH_ITEM = 45;        // минимальная ширина элемента графа
    private final float BOTTOM_PEDDING = 5;         // отступ снизу
    private final float BETWEEN_DATES = 20;         // расстояние между отображением дней

    private final int COUNT_VISIBLE_ITEM = 5;         // кол-во видимых элементов температуры

    private int mBackgroudColor = 0xFF061D27;
    private int mBorderColor = Color.BLACK;
    private int mTextDaysColor = 0xFF687885;
    private int mTextTimeColor = 0xFFF4F8F9;
    private int mTextTemperatureColor = 0xFFF4F8F9;

    private float mCanvaWigth;
    private float mCanvaHeight;
    private float mCanvaOffset = 0;
    private float mGistogramWidth;
    private float mCurrentItemWidth;
    private boolean mCheckBeginItem = true;

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
    private boolean mShowSecondGistogram = false; // показать вторую гистограмму
    private int firstVisibleItem = -1;
    private int oldFirstVisibleItem = -1;

    private void inicialisation(Context context){

        borderPaint = new Paint();
        borderPaint.setColor(mBorderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_WIDTH);

        mDetector = new GestureDetector(context, new GismeteoViewGestureListener());
        mScroller = new Scroller(context);

        setHorizontalScrollBarEnabled(true);

        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
        initializeScrollbars(a);
        a.recycle();

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
        tempTextP.setColor(mTextTemperatureColor);

        // кисть для текста времени
        hourTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourTextP.setStyle(Paint.Style.STROKE);
        hourTextP.setTextSize(TIME_TEXT_SIZE);
        hourTextP.setTextAlign(Paint.Align.CENTER);
        hourTextP.setColor(mTextTimeColor);

        // кисть для текста даты
        dateTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextP.setStyle(Paint.Style.STROKE);
        dateTextP.setTextSize(DATE_TEXT_SIZE);
        dateTextP.setTextAlign(Paint.Align.LEFT);
        dateTextP.setColor(mTextDaysColor);

        // кисть для текста с правым выравниванием
        dateTextPRight = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextPRight.setStyle(Paint.Style.STROKE);
        dateTextPRight.setTextSize(DATE_TEXT_SIZE);
        dateTextPRight.setTextAlign(Paint.Align.RIGHT);
        dateTextPRight.setColor(mTextDaysColor);

        // кисть для закрашивания надписи в цвет фона
        fillText = new Paint();
        fillText.setStyle(Paint.Style.FILL_AND_STROKE);
        fillText.setColor(mBackgroudColor);

        return;
    }

    /**
     * показать/скрыть температуру
     * @param showTemperature
     */
    public void setShowTemperature(boolean showTemperature) {
        this.mShowTemperature = showTemperature;
        invalidate();
    }

    public void setShowSecondGistogram(boolean ShowSecondGistogram) {
        this.mShowSecondGistogram = ShowSecondGistogram;
        calculateItemParam();
        invalidate();
    }

    public GisMeteoView(Context context) {
        super(context);
        calculateItemParam();
        inicialisation(context);
    }

    public GisMeteoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        calculateItemParam();
        inicialisation(context);
    }

    public GisMeteoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        calculateItemParam();
        inicialisation(context);
    }

    public void setTemperatureAdapter(ArrayList<HashMap<String, String>> adapter){
        mTemperatureAdapter = new ArrayList<TemperatureItem>();
        Random rand  = new Random();
        // формируем список элементов
        for(HashMap<String, String> item: adapter){
            mTemperatureAdapter.add(new TemperatureItem(item.get(tagHour),
                                                        item.get(tagDate),
                                                        Integer.parseInt(item.get(tagTemperature)),
                                                        Integer.parseInt(item.get(tagTemperature)) - rand.nextInt(10)));
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
        if ((mCanvaHeight == 0) || (mCanvaWigth == 0)){
            return;
        }
        TemperatureItem item;
        float maxTemp = -999, minTemp = 999, temp;
        float deltaY = 0;

        // ширина элемента
        mCurrentItemWidth = (mCanvaWigth - 2 * BORDER_WIDTH) / COUNT_VISIBLE_ITEM;
        if (mCurrentItemWidth < MIN_WIDTH_ITEM) {
            mCurrentItemWidth = MIN_WIDTH_ITEM;
        }

        // определяем максимальную и минимальную температуру из имеющихся  в основном списке
        for (int i = 0; i < mTemperatureAdapter.size(); i++){
            temp = mTemperatureAdapter.get(i).temperature;
            maxTemp = maxTemp < temp ? temp : maxTemp;
            minTemp = minTemp > temp ? temp : minTemp;
        }

        // если показываем вторую гистограмму то и по ее списку пробегаемся
        if (mShowSecondGistogram){
            for (int i = 0; i < mTemperatureAdapter.size(); i++){
                temp = mTemperatureAdapter.get(i).second_temperature;
                maxTemp = maxTemp < temp ? temp : maxTemp;
                minTemp = minTemp > temp ? temp : minTemp;
            }
        }

        // число пикселей по оси Y на 1 градус
        float infoZoneHeight = DATE_TEXT_SIZE + TIME_TEXT_SIZE + TEMPERATURE_TEXT_SIZE
                               + BORDER_WIDTH
                               + SPACE_UNDER_TEMPERATURE_TEXT + SPACE_UNDER_DATE_TEXT + SPACE_UNDER_TIME_TEXT;
        float gistogram_height = mCanvaHeight - infoZoneHeight - (BOTTOM_PEDDING + BORDER_WIDTH);
        if (mShowSecondGistogram){
            gistogram_height -= TEMPERATURE_TEXT_SIZE + SPACE_UNDER_TEMPERATURE_TEXT;
        }

        // если не хватает места
        if (gistogram_height < 100) {
            gistogram_height = 100;
        }

        if (Math.abs(maxTemp - minTemp) != 0) {
            deltaY = gistogram_height / Math.abs(maxTemp - minTemp);
        }

        for (int i = 0; i < mTemperatureAdapter.size(); i++){
            item = mTemperatureAdapter.get(i);
            item.startX = i * mCurrentItemWidth;
            item.endX = item.startX + mCurrentItemWidth;
            item.Y = gistogram_height - (float)Math.ceil(deltaY * Math.abs(item.temperature - minTemp)) +
                    infoZoneHeight;
            item.showColor = getTemperatureColor(item.temperature);
            if (mShowSecondGistogram) {
                item.second_Y = gistogram_height - (float) Math.ceil(deltaY * Math.abs(item.second_temperature - minTemp)) +
                        infoZoneHeight;
                item.second_showColor = getTemperatureColor(item.second_temperature);
            }
        }

        mGistogramWidth = mTemperatureAdapter.size() * mCurrentItemWidth + 2 * BORDER_WIDTH;
        // после поворота
        if (oldFirstVisibleItem > -1){
            mCanvaOffset = mTemperatureAdapter.get(oldFirstVisibleItem).startX;
            oldFirstVisibleItem = -1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCanvaHeight = getMeasuredHeight();
        mCanvaWigth = getMeasuredWidth();
        calculateItemParam();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // закрашиваем фон
        canvas.drawColor(mBackgroudColor);

        // если находимся в режиме разработки то дальше не идем
        if (isInEditMode()){
            return;
        }

        // если скроллинг не закончился то получаем новые координаты
        if (mScroller.computeScrollOffset()){
            mCanvaOffset = mScroller.getCurrX();
        }

        TemperatureItem item = null, prev_item = null, prevDrawDate= null;
        // определяем первый видимый элемент
        for (int i = 0; i < mTemperatureAdapter.size(); i++){
            if (mTemperatureAdapter.get(i).endX > mCanvaOffset){
                firstVisibleItem = i;
                oldFirstVisibleItem = i;
                break;
            }
        }

        canvas.save();
        // сдвигаем канву на необходимое расстояние
        canvas.translate(-mCanvaOffset, 0);

        for (int i = firstVisibleItem; (i < mTemperatureAdapter.size()) && (mTemperatureAdapter.get(i).startX < (mCanvaWigth + mCanvaOffset)); i++){
            item = mTemperatureAdapter.get(i);
            lineP.setColor(item.showColor);
            canvas.drawLine(item.startX, item.Y, item.endX, item.Y, lineP);
            if (mShowSecondGistogram){
                lineP.setColor(item.second_showColor);
                canvas.drawLine(item.startX, item.second_Y, item.endX, item.second_Y, lineP);
            }

            if (i > firstVisibleItem){
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
                if (mShowSecondGistogram){
                    if (prev_item.second_temperature > item.second_temperature) {
                        v_lineP.setShader(new LinearGradient(item.startX,
                                prev_item.second_Y - LINE_WIDTH / 2.0f,
                                item.startX,
                                item.second_Y + LINE_WIDTH / 2.0f,
                                prev_item.second_showColor,
                                item.second_showColor,
                                Shader.TileMode.CLAMP));
                        canvas.drawLine(item.startX, prev_item.second_Y - LINE_WIDTH / 2.0f, item.startX, item.second_Y + LINE_WIDTH / 2.0f, v_lineP);
                    }
                    else {
                        v_lineP.setShader(new LinearGradient(item.startX,
                                prev_item.second_Y + LINE_WIDTH / 2.0f,
                                item.startX,
                                item.second_Y - LINE_WIDTH / 2.0f,
                                prev_item.second_showColor,
                                item.second_showColor,
                                Shader.TileMode.CLAMP));
                        canvas.drawLine(item.startX, prev_item.second_Y + LINE_WIDTH / 2.0f, item.startX, item.second_Y - LINE_WIDTH / 2.0f, v_lineP);
                    }
                }
            }

            // выводим дату
            if ((prev_item == null) || (item.day.compareToIgnoreCase(prev_item.day)) != 0){
                float begin_x = prevDrawDate == null ? mCanvaOffset + BORDER_WIDTH : item.startX + BORDER_WIDTH;
                if (prevDrawDate != null){
                    float prevTextWidth = dateTextP.measureText(prevDrawDate.day);
                    if ((mCanvaOffset + BORDER_WIDTH + prevTextWidth >= begin_x - BETWEEN_DATES)){
                        // закрашиваем предыдущий текст
                        canvas.drawRect(BORDER_WIDTH,
                                        BORDER_WIDTH,
                                        mCanvaOffset + BORDER_WIDTH + prevTextWidth + BETWEEN_DATES,
                                        BORDER_WIDTH + DATE_TEXT_SIZE + dateTextP.getFontMetrics().descent,
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

            // выводим время
            canvas.drawText(item.hour,
                    item.startX + (item.endX - item.startX) / 2,
                    BORDER_WIDTH + DATE_TEXT_SIZE + SPACE_UNDER_DATE_TEXT + TIME_TEXT_SIZE,
                    hourTextP);

            // выводим температуру если нужно
            if(mShowTemperature){
                String temp_str = String.valueOf(item.temperature);
                if (item.temperature > 0) temp_str = "+" + temp_str;
                if (item.temperature < 0) temp_str = "-" + temp_str;

                canvas.drawText(temp_str,
                        item.startX + (item.endX - item.startX) / 2,
                        item.Y - SPACE_UNDER_TEMPERATURE_TEXT,
                        tempTextP);

                if (mShowSecondGistogram){
                    String second_temp_str = String.valueOf(item.second_temperature);
                    if (item.second_temperature > 0) second_temp_str = "+" + second_temp_str;
                    if (item.second_temperature < 0) second_temp_str = "-" + second_temp_str;

                    canvas.drawText(second_temp_str,
                            item.startX + (item.endX - item.startX) / 2,
                            item.second_Y + TEMPERATURE_TEXT_SIZE,
                            tempTextP);
                }
            }
        }

        if (!mScroller.isFinished()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        // рамка
        canvas.drawRect(BORDER_WIDTH / 2 + mCanvaOffset, BORDER_WIDTH / 2, mCanvaWigth - BORDER_WIDTH / 2 + mCanvaOffset, mCanvaHeight - BORDER_WIDTH / 2, borderPaint);
        canvas.restore();

        // доводим гистограмму до начала элемента
        if (mScroller.isFinished() & mCheckBeginItem) {
            mCheckBeginItem = false;
            if ((mTemperatureAdapter == null) || (mTemperatureAdapter.size() == 0)) {
                return;
            }

            int last_startX = (int) mTemperatureAdapter.get(mTemperatureAdapter.size() - COUNT_VISIBLE_ITEM).startX;
            if (mCanvaOffset < mTemperatureAdapter.get(0).startX) {
                mScroller.fling(0, 0, 1000, 0, 0, 0, 0, 0);
                oldFirstVisibleItem = 0;
            } else if (mCanvaOffset > last_startX) {
                mScroller.fling(last_startX, 0, 1000, 0, last_startX, last_startX, 0, 0);
                oldFirstVisibleItem = mTemperatureAdapter.size() - COUNT_VISIBLE_ITEM;
            } else if (mTemperatureAdapter.get(firstVisibleItem).startX < mCanvaOffset) {
                mScroller.fling((int) mTemperatureAdapter.get(firstVisibleItem).endX,
                        0,
                        20,
                        0,
                        (int) mTemperatureAdapter.get(firstVisibleItem).endX,
                        (int) mTemperatureAdapter.get(firstVisibleItem).endX,
                        0,
                        0);
                oldFirstVisibleItem = firstVisibleItem;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        mCheckBeginItem = (event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_CANCEL);
        return true;
    }


    private class GismeteoViewGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // при выходе за границы гистограммы замедляем скрол в 2 раза
            if (((mCanvaOffset + distanceX) < 0) || ((mCanvaOffset + distanceX) > mGistogramWidth - mCanvaWigth)) {
                mCanvaOffset += distanceX / 2;
            }
            else {
                mCanvaOffset += distanceX;
            }
            if(mCanvaOffset < - mCanvaWigth) {
                mCanvaOffset = 0;
            }
            if(mCanvaOffset > mGistogramWidth){
                mCanvaOffset = mGistogramWidth;
            }
            if (!awakenScrollBars()) {
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling((int)mCanvaOffset, 0, (int)-velocityX, 0, 0, (int)(mGistogramWidth - mCanvaWigth), 0, 0);
            awakenScrollBars(mScroller.getDuration());
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            return true;
        }
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return (int)mCanvaWigth;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return (int)mCanvaOffset;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return (int)mGistogramWidth;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SaveStateGismeteo st = new SaveStateGismeteo(super.onSaveInstanceState());

        st.firstVisibleItem = oldFirstVisibleItem;
        st.showTemperature = mShowTemperature;
        st.showSecondGistogram = mShowSecondGistogram;
        return st;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SaveStateGismeteo)){
            super.onRestoreInstanceState(state);
            return;
        }

        SaveStateGismeteo ssg = (SaveStateGismeteo)state;
        super.onRestoreInstanceState(ssg.getSuperState());

        mShowTemperature = ssg.showTemperature;
        mShowSecondGistogram = ssg.showSecondGistogram;
        oldFirstVisibleItem = ssg.firstVisibleItem;
        calculateItemParam();
    }

    public static class SaveStateGismeteo extends BaseSavedState{

        int firstVisibleItem;
        boolean showTemperature;
        boolean showSecondGistogram;

        public SaveStateGismeteo(Parcelable superState) {
            super(superState);
        }

        public SaveStateGismeteo(Parcel source) {
            super(source);
            firstVisibleItem = source.readInt();
            showTemperature = source.readByte() == 1;
            showSecondGistogram = source.readByte() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(firstVisibleItem);
            dest.writeByte((byte) (showTemperature ? 1 : 0));
            dest.writeByte((byte) (showSecondGistogram ? 1 : 0));

        }

        public static final Parcelable.Creator<SaveStateGismeteo> CREATOR = new Parcelable.Creator<SaveStateGismeteo>(){
            @Override
            public SaveStateGismeteo createFromParcel(Parcel source) {
                return new SaveStateGismeteo(source);
            }

            @Override
            public SaveStateGismeteo[] newArray(int size) {
                return new SaveStateGismeteo[size];
            }
        };
    }
}
