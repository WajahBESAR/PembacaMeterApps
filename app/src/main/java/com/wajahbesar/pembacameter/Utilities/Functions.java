package com.wajahbesar.pembacameter.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Functions {

    private Context context;

    public Functions(Context context) {
        this.context = context;
    }

    public void showMessage(View view, String strPrefix, String strPostfix, int msDuration) {
        SpannableStringBuilder snackbarText = new SpannableStringBuilder();
        snackbarText.append(strPrefix);
        int boldStart = snackbarText.length();
        snackbarText.append(strPostfix);
        snackbarText.setSpan(new ForegroundColorSpan(Color.YELLOW), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        snackbarText.append(".");

        Snackbar snackbar = Snackbar.make(view, snackbarText, Snackbar.LENGTH_LONG).setDuration(msDuration).setBackgroundTint(Color.RED);
        snackbar.show();
    }

    public void Getar() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } else {
            if (vibrator != null) {
                vibrator.vibrate(20);
            }
        }
    }

}
