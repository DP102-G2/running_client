package com.gt.running_client;

import android.content.Context;
import android.widget.Toast;

public class Common {

    /* Toast */
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
