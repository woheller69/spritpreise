package org.woheller69.spritpreise.ui.Help;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;

import androidx.preference.PreferenceManager;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Boolean.TRUE;

import org.woheller69.spritpreise.R;

public final class StringFormatUtils {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private static final DecimalFormat intFormat = new DecimalFormat("0");

    public static SpannableString formatPrice(Context context, String prefix, Double price, String suffix){
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        format.applyPattern("0.000");
        String pricestring = format.format(price);
        SpannableString priceformat = new SpannableString(prefix + pricestring + suffix);
        priceformat.setSpan(new TextAppearanceSpan(context, android.R.style.TextAppearance_Small), priceformat.length()-3, priceformat.length()-2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceformat.setSpan(new SuperscriptSpan(), priceformat.length()-3, priceformat.length()-2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceformat.setSpan(new ForegroundColorSpan(context.getColor(R.color.colorPrimaryDark)), 0, priceformat.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return priceformat;
    }

    public static String formatDecimal(float decimal) {
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return removeMinusIfZerosOnly(decimalFormat.format(decimal));
    }

    public static String formatInt(float decimal) {
        intFormat.setRoundingMode(RoundingMode.HALF_UP);
        return removeMinusIfZerosOnly(intFormat.format(decimal));
    }

    public static String formatInt(float decimal, String appendix) {
        return String.format("%s\u200a%s", removeMinusIfZerosOnly(formatInt(decimal)), appendix); //\u200a adds tiny space
    }

    public static String formatDecimal(float decimal, String appendix) {
        return String.format("%s\u200a%s", removeMinusIfZerosOnly(formatDecimal(decimal)), appendix);
    }

    public static String formatTimeWithoutZone(Context context, long time) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SimpleDateFormat tf;
        java.text.DateFormat df = java.text.DateFormat.getDateInstance(DateFormat.SHORT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (android.text.format.DateFormat.is24HourFormat(context) || sharedPreferences.getBoolean("pref_TimeFormat", true)==TRUE){
            tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tf.setTimeZone(TimeZone.getTimeZone("GMT"));
        }else {
            tf = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            tf.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return df.format(time)+" "+tf.format(time);
    }



    public static String removeMinusIfZerosOnly(String string){
        // It removes (replaces with "") the minus sign if it's followed by 0-n characters of "0.00000...",
        // so this will work for any similar result such as "-0", "-0." or "-0.000000000"
        // https://newbedev.com/negative-sign-in-case-of-zero-in-java
        return string.replaceAll("^-(?=0(\\.0*)?$)", "");
    }
}
