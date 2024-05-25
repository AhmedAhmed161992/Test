package learn.draw;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by sonu on 15/8/18.
 */

public class MyReceiver extends BroadcastReceiver {
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;
    AlertDialog.Builder builder;
    private AlertDialog alertdialog;


    @Override
    public void onReceive(final Context context, Intent intent) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            networkInfo  =connectivityManager.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()){

                if (alertdialog!=null) {
                    alertdialog.dismiss();

                }
            }
            else {

                builder =new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.internetconnectiondialog,null);
                builder.setView(view);
                builder.setCancelable(false);
                builder.create();
                alertdialog =  builder.show();
                view.findViewById(R.id.cancel_internet)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertdialog.dismiss();

                            }
                        });
                view.findViewById(R.id.interner_settings)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        });

            }
        }

    }
}

