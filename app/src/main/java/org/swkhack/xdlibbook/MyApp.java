package org.swkhack.xdlibbook;

import android.app.Application;

/**
 * Created by swk on 2/28/16.
 */
public class MyApp extends Application {
    private getBookDateTask bookDateTask;
    private MyDatabaseHelper dbHelper;

    public MyDatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(MyDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public getBookDateTask getBookDateTask() {
        return bookDateTask;
    }

    public void setBookDateTask(getBookDateTask bookDateTask) {
        this.bookDateTask = bookDateTask;
    }
}
