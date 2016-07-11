package com.example.rafa.liquidgalaxypoiscontroller.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.rafa.liquidgalaxypoiscontroller.R;
import com.example.rafa.liquidgalaxypoiscontroller.beans.POI;
import com.jcraft.jsch.JSchException;

import java.util.List;

/**
 * Created by lgwork on 7/07/16.
 */
public class PoisGridViewAdapter extends BaseAdapter {

    List<POI> poiList;
    Context context;
    Activity activity;

    public PoisGridViewAdapter(List<POI> poiList, Context context, Activity activity) {
        this.poiList = poiList;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return this.poiList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.poiList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return this.poiList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final POI currentPoi = this.poiList.get(i);
        Button button = new Button(context);
        button.setText(currentPoi.getName());

        Drawable top = context.getResources().getDrawable(R.drawable.ic_place_black_24dp);
        button.setCompoundDrawablesWithIntrinsicBounds(top, null, null, null);

        button.setBackground(context.getResources().getDrawable(R.drawable.button_rounded_grey));
        button.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = buildCommand(currentPoi);
                VisitPoiTask visitPoiTask = new VisitPoiTask(command);
                visitPoiTask.execute();
            }
        });

        return button;
    }


    private String buildCommand(POI poi) {

        return "echo 'flytoview=<LookAt><longitude>" + poi.getLongitude() +
                "</longitude><latitude>" + poi.getLatitude() +
                "</latitude><altitude>" + poi.getAltitude() +
                "</altitude><heading>" + poi.getHeading() +
                "</heading><tilt>" + poi.getTilt() +
                "</tilt><range>" + poi.getRange() +
                "</range><gx:altitudeMode>" + poi.getAltitudeMode() +
                "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
    }

    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        String command;
        private ProgressDialog dialog;

        public VisitPoiTask(String command) {
            this.command = command;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                dialog.setMessage(context.getResources().getString(R.string.viewing_poi));
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                dialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return LGUtils.setConnectionWithLiquidGalaxy(command, activity);
            } catch (JSchException e) {
                this.cancel(true);
                if (dialog != null) {
                    dialog.dismiss();
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, context.getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }
        }


        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            if (success != null) {
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
        }

    }

}