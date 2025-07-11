package com.ramson.appmachines.silverscreenratings.util;

import android.app.AlertDialog;
import android.content.Context;

public class LoadingDialog {

    private static AlertDialog dialog;
    private static AlertDialog.Builder builder;

    public static void show(Context context, String title, boolean isCancelable) {
        builder = new AlertDialog.Builder(context);
        dialog = builder.create();
        dialog.setTitle(title);
        dialog.setCancelable(isCancelable);
        dialog.show();
    }

    public static void cancel() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
            builder = null;
        }
    }
}
