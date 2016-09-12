package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStockDetail;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by stark on 10/9/16.
 */
public class DetailWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = DetailWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //Create Intent to launch MyStocksActivity
            Intent intent = new Intent(context, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            setRemoteAdapter(context, remoteViews);

            Intent clickIntentTemplate = new Intent(context, MyStockDetail.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

            //Perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(@NonNull Context context,@NonNull Intent intent) {
        super.onReceive(context, intent);
        if(context.getString(R.string.ACTION_DATA_UPDATED).equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass())
            );
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        } else {
        }
    }

    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views){
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, DetailWidgetRemoteViewService.class));
    }

}
