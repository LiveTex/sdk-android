package ru.livetex.demoapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class IntentUtils {

    public static void goUrl(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }
}
