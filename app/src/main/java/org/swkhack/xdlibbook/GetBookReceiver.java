package org.swkhack.xdlibbook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by swk on 2/28/16.
 */
public class GetBookReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, GetBookService.class);
        context.startService(i);

    }
}
