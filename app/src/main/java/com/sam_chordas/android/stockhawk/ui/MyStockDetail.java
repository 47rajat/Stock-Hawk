package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.text.DecimalFormat;

public class MyStockDetail extends AppCompatActivity {
    private static final String LOG_TAG = MyStockDetail.class.getSimpleName();
    private String[] mLabelsLand;
    private String[] mLabelsPort;
    private float[] mValuesLand;
    private float[] mValuesPort;
    private LineSet mDataSet;
    private static final String STOCK_LABEL_KEY_LAND = "stock_labels land";
    private static final String STOCK_VALUES_KEY_LAND = "stock_values land";
    private static final String STOCK_VALUES_KEY_PORT = "stock values port";
    private static final String STOCK_LABEL_KEY_PORT = "stock labels port";
    private int mMinimumStockValues;
    private int mMaximumStockValues;
    private static final String STOCK_MAX_KEY = "stock_max_value";
    private static final String STOCK_MIN_KEY = "stock_min_value";
    private static final int MAX_DATA = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stock_detail);
        if(!Utils.isOnline(this)){
            networkToast();
        }

        TextView textView = (TextView)findViewById(R.id.detail_stock_symbol);
        textView.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        textView.setContentDescription(getString(R.string.a11y_stock_symbol, getIntent().getStringExtra(Intent.EXTRA_TEXT)));
        if (savedInstanceState == null) {
            String symbol = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            Cursor cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns.BIDPRICE}, QuoteColumns.SYMBOL + "= ?",
                    new String[]{symbol}, null);
            if(cursor != null){
                cursor.moveToLast();
            }
            final int SIZE_LAND = cursor.getCount() < MAX_DATA ? cursor.getCount(): MAX_DATA;
            final int SIZE_PORT = cursor.getCount() < MAX_DATA/2 ? cursor.getCount() : MAX_DATA/2;
            mLabelsLand = new String[SIZE_LAND];
            mValuesLand = new float[SIZE_LAND];
            mValuesPort = new float[SIZE_PORT];
            mLabelsPort = new String[SIZE_PORT];
            mMinimumStockValues = -1;
            mMaximumStockValues = -1;
            int i = 0;
            int j = 0;
            do {
                mValuesLand[SIZE_LAND - i -1] = Float.parseFloat(cursor.getString(0));
                if(j < MAX_DATA/2) {
                    mValuesPort[SIZE_PORT - j - 1] = Float.parseFloat(cursor.getString(0));
                }
                if (mMinimumStockValues == -1) {
                    mMinimumStockValues = (int) mValuesLand[SIZE_LAND - i -1];
                } else if (mValuesLand[SIZE_LAND - i -1] < mMinimumStockValues) {
                    mMinimumStockValues = (int) mValuesLand[SIZE_LAND - i -1];
                }

                if (mMaximumStockValues == -1) {
                    mMaximumStockValues = (int) mValuesLand[SIZE_LAND - i -1] + 1;
                } else if (mValuesLand[SIZE_LAND - i -1] > mMaximumStockValues) {
                    mMaximumStockValues = (int) mValuesLand[MAX_DATA - i -1] + 1;
                }
                mLabelsLand[i++] = "" + i ;
                if(j < SIZE_PORT){
                    mLabelsPort[j++] = ""+ j;
                }
            } while (cursor.moveToPrevious() && i < SIZE_LAND);
            cursor.close();
        } else{
            if(savedInstanceState.containsKey(STOCK_VALUES_KEY_LAND) && savedInstanceState.containsKey(STOCK_LABEL_KEY_LAND)
                    && savedInstanceState.containsKey(STOCK_MAX_KEY) && savedInstanceState.containsKey(STOCK_MIN_KEY)){
                mMaximumStockValues = savedInstanceState.getInt(STOCK_MAX_KEY);
                mMinimumStockValues = savedInstanceState.getInt(STOCK_MIN_KEY);
                mValuesLand = savedInstanceState.getFloatArray(STOCK_VALUES_KEY_LAND);
                mLabelsLand = savedInstanceState.getStringArray(STOCK_LABEL_KEY_LAND);
                mLabelsPort = savedInstanceState.getStringArray(STOCK_LABEL_KEY_PORT);
                mValuesPort = savedInstanceState.getFloatArray(STOCK_VALUES_KEY_PORT);
            }
        }

        if(getResources().getBoolean(R.bool.is_landscape)) {
            mDataSet = new LineSet(mLabelsLand, mValuesLand);
        } else{
            mDataSet = new LineSet(mLabelsPort, mValuesPort);
        }
        mDataSet.setColor(getResources().getColor(R.color.material_blue_500));
        LineChartView lineChartView = (LineChartView) findViewById(R.id.linechart);
        lineChartView.setContentDescription(getString(R.string.detail_stock_price_chart));
        lineChartView.addData(mDataSet);
        lineChartView.setLabelsFormat(new DecimalFormat("##0.00" ));




        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.material_green_700));




        lineChartView.setGrid(ChartView.GridType.HORIZONTAL,gridPaint);
        lineChartView.setAxisBorderValues(mMinimumStockValues -1, mMaximumStockValues);
        lineChartView.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mLabelsLand.length >0){
            outState.putStringArray(STOCK_LABEL_KEY_LAND, mLabelsLand);
        }
        if(mValuesLand.length > 0){
            outState.putFloatArray(STOCK_VALUES_KEY_LAND, mValuesLand);
        }
        if(mMaximumStockValues != -1){
            outState.putInt(STOCK_MAX_KEY, mMaximumStockValues);
        }
        if(mMinimumStockValues != -1){
            outState.putInt(STOCK_MIN_KEY, mMinimumStockValues);
        }
        if(mLabelsPort.length > 0){
            outState.putStringArray(STOCK_LABEL_KEY_PORT, mLabelsPort);
        }
        if(mValuesPort.length > 0){
            outState.putFloatArray(STOCK_VALUES_KEY_PORT, mValuesPort);
        }
        super.onSaveInstanceState(outState);
    }
    public void networkToast(){
        Toast.makeText(this, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }
}
