package lav.testgismeteo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;

import lav.mycomponent.GisMeteoView;


public class activity_main extends ActionBarActivity {

    GisMeteoView gisMeteoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gisMeteoView = (GisMeteoView)findViewById(R.id.Gismeteo);

        ArrayList<HashMap<String, String>> testData = new ArrayList<HashMap<String, String>>();

        String[] days = getResources().getStringArray(R.array.days);
        String[] hours = getResources().getStringArray(R.array.hours);
        String[] temper = getResources().getStringArray(R.array.temperaturies);

        HashMap<String, String> item;
        for (int i = 0; i < days.length; i++){
            item = new HashMap();
            item.put(GisMeteoView.tagDate, days[i]);
            item.put(GisMeteoView.tagHour, hours[i]);
            item.put(GisMeteoView.tagTemperature, temper[i]);
            testData.add(item);
        }

        gisMeteoView.setTemperatureAdapter(testData);
    }

    public void cbShowTemperature(View view) {
        gisMeteoView.setShowTemperature(((CheckBox)view).isChecked());
    }

    public void cbShowSecondGistogram(View view) {
        gisMeteoView.setShowSecondGistogram(((CheckBox)view).isChecked());
    }
}
