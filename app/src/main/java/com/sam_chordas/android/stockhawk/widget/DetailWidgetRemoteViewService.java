package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by stark on 10/9/16.
 */
public class DetailWidgetRemoteViewService extends RemoteViewsService {
    private static final String LOG_TAG = DetailWidgetRemoteViewService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return  data == null? 0: data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if(i == AdapterView.INVALID_POSITION
                        || data == null || !data.moveToPosition(i)){
                    return  null;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String change;
                if (Utils.showPercent){
                    change = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                    remoteViews.setContentDescription(R.id.widget_change, getString(R.string.a11y_stock_price_percent_change, change));
                } else{
                    change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
                    remoteViews.setContentDescription(R.id.widget_change, getString(R.string.a11y_stock_price_absolute_change, change));
                }

                remoteViews.setTextViewText(R.id.widget_stock_symbol, data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
                remoteViews.setContentDescription(R.id.widget_stock_symbol, getString(R.string.a11y_stock_symbol, data.getString(data.getColumnIndex(QuoteColumns.SYMBOL))));
                remoteViews.setTextViewText(R.id.widget_bid_price, data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
                remoteViews.setContentDescription(R.id.widget_bid_price, getString(R.string.a11y_stock_price, data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE))));

                if (data.getInt(data.getColumnIndex(QuoteColumns.ISUP)) == 1){
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);

                } else{
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                remoteViews.setTextViewText(R.id.widget_change, change);
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Intent.EXTRA_TEXT, data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if(data.moveToPosition(i))
                    return data.getLong(data.getColumnIndex(QuoteColumns._ID));
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
