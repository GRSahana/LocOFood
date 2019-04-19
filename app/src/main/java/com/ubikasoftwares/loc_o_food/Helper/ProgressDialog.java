package com.ubikasoftwares.loc_o_food.Helper;

import android.app.AlertDialog;
import android.content.Context;

import dmax.dialog.SpotsDialog;

public class ProgressDialog {

    private static AlertDialog pDialog;

    public static void setProgressDialog(Context context){
        pDialog = new SpotsDialog.Builder().
                setContext(context).
                setCancelable(false).
                setMessage("Loading").
                build();
        pDialog.show();
    }

    public static void hideProgressDialog(){
        if(pDialog!=null){
            pDialog.dismiss();
        }
    }
}
