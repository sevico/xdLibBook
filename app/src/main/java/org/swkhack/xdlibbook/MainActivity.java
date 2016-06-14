package org.swkhack.xdlibbook;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    public static final int getData_success = 1;
    public static final int getData_failed = 0;
    private MyDatabaseHelper dbHelper;
    private Button login_btn;
    private TextView number_text;
    private TextView password_text;
    private getBookDateTask myGet;
    private MyApp myApp;
    private int notifyId;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            db.delete("Book", null, null);

            switch (msg.what) {
                case getData_success:

                    for (Book b : myGet.getBooks()) {
                        values.put("author", b.getAuthor());
                        values.put("name", b.getName());
                        values.put("year", b.getYear());
                        values.put("retDate", b.getOriDate());
                        db.insert("Book", null, values);
                        values.clear();
                    }
                    CompareAndNotify();
                    break;
                case getData_failed:
                    Toast.makeText(MainActivity.this, R.string.get_failed, Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    public MyDatabaseHelper getDbHelper() {

        return dbHelper;
    }
    private void startBookService() {
        Intent intent = new Intent(this, GetBookService.class);


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("login_info", MODE_PRIVATE);
        String number = pref.getString("number", "");
        String password = pref.getString("password", "");
        number_text = (TextView) findViewById(R.id.number);
        password_text = (TextView) findViewById(R.id.password);

        if (number != "" && password != "") {
            number_text.setText(number);
            password_text.setText(password);
        }
        login_btn = (Button) findViewById(R.id.login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = number_text.getText().toString();
                String password = password_text.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("login_info", MODE_PRIVATE).edit();
                editor.putString("number", number);
                editor.putString("password", password);
                editor.commit();
                MyApp myapp = (MyApp) getApplication();
                dbHelper = new MyDatabaseHelper((MainActivity.this), "bookList.db", null, 2);
//                Toast.makeText(getApplicationContext(),"Test", Toast.LENGTH_SHORT).show();
//                spider my_spider = new spider("1503121612", "000000", dbHelper);
                myGet=new getBookDateTask(number, password, dbHelper,handler);
                GetBookService.myGet = myGet;
                GetBookService.dbHelper = dbHelper;
                Intent intent = new Intent(MainActivity.this, GetBookService.class);
                startService(intent);

//                myGet.execute();
//                my_spider.getBookData();
//                Log.d("swkTest", "test");
            }
        });
    }
    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }
    private void notify(Book book) {
        String sTitle = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");

        if (book.getDays() > 0) {
            sTitle = "你有书要归还啦";
        } else if (book.getDays() == 0) {
            sTitle = "你有书今日要归还";
        }
        else{
            sTitle = "你有书已超期";
        }
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(sTitle)//设置通知栏标题
                .setContentText(book.getName()+"归还日期: "+formatter.format(book.getRetDate())) //<span style="font-family: Arial;">/设置通知栏显示内容</span>
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) //设置通知栏点击意图
                .setTicker("书籍归还信息") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.bang);//设置通知小ICON
        manager.notify(notifyId++, mBuilder.build());

    }
    private void CompareAndNotify() {
        Comparator<Book> comparator = new Comparator<Book>(){
            @Override
            public int compare(Book book, Book t1) {
                return (int)(book.getDays() - t1.getDays());
            }
        };
        ArrayList<Book> books = myGet.getBooks();
        Collections.sort(books, comparator);
        int i=0;
        for (i=0;i<books.size();i++) {
            if (books.get(i).getDays() >= 0) {
                break;
            }
            notify(books.get(i));

        }
        for (;i<books.size();i++) {
            if (books.get(i).getDays() > 0) {
                break;
            }
            notify(books.get(i));
        }
        int j=i;
        for (;i<books.size() && i<j+3;i++) {
            notify(books.get(i));
        }
    }
}
