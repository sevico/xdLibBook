package org.swkhack.xdlibbook;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by swk on 2/26/16.
 */
public class getBookDateTask extends AsyncTask<Void, Integer, Boolean> {
    private String libPath = "http://al.lib.xidian.edu.cn/F?RN=123456";
    private String number;
    private String password;
    private MyDatabaseHelper dbHelper;
    private ArrayList<Book> books;
    private Handler handler;
    public ArrayList<Book> getBooks() {
        return books;
    }


    public getBookDateTask(String number, String password, MyDatabaseHelper db,Handler handler) {
        this.number = number;
        this.password = password;
        books = new ArrayList<Book>();
        this.dbHelper = db;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(Void... users) {
        try {
            URL url = new URL(libPath);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int a = con.getResponseCode();
            if (con.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                org.jsoup.nodes.Document doc = Jsoup.parse(response.toString());
                Elements aTag = doc.select("a[title=输入用户名和密码]");
                if (aTag.size()==0) {
                    Log.e("swkError", "未找到a登录节点");
                }
                String loginStr = aTag.attr("href").toString();
//                        Log.d("swkDebug", aTag.attr("href").toString());
                HttpClient client = new DefaultHttpClient();
                NameValuePair func = new BasicNameValuePair("func", "login-session");
                NameValuePair login_source = new BasicNameValuePair("login_source", "bor-info");
                NameValuePair bor_verification = new BasicNameValuePair("bor_verification", password);
                NameValuePair bor_id = new BasicNameValuePair("bor_id", number);
                NameValuePair lib = new BasicNameValuePair("bor_library", "XDU50");
                ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(func);
                pairs.add(login_source);
                pairs.add(bor_id);
                pairs.add(bor_verification);
                pairs.add(lib);

                HttpPost httpPost = new HttpPost(loginStr);
                HttpEntity requestEntiy = new UrlEncodedFormEntity(pairs);
                httpPost.setEntity(requestEntiy);
                HttpResponse responsePost = client.execute(httpPost);
                if (responsePost.getStatusLine().getStatusCode() == 200) {
//                            Log.d("swkDebug", "Post Success!");
                    HttpEntity entity = responsePost.getEntity();
                    String responseStr = EntityUtils.toString(entity, "utf-8");

                    if (responseStr.indexOf("证号或密码") != -1) {
                        Log.e("swkDebug", "帐号或密码错误!");
                        return false;
                    }
                    else{
                        Log.d("swkDebug", "登录成功!");
                    }
                }
                String bookBorrowedList_url = loginStr.substring(0, loginStr.indexOf("?")) + "?func=bor-loan&adm_library=XDU50";
//                        Log.d("swkDebug", bookBorrowedList_url);
                URL urlBorrowed = new URL(bookBorrowedList_url);
                HttpURLConnection conBorrowedPage = (HttpURLConnection) urlBorrowed.openConnection();
                conBorrowedPage.setRequestMethod("GET");
                if (conBorrowedPage.getResponseCode() == 200) {
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(conBorrowedPage.getInputStream()));
                    StringBuilder responseBorrowed = new StringBuilder();
                    while ((inputLine = in2.readLine()) != null) {
                        responseBorrowed.append(inputLine);
                    }
                    in2.close();
                    String reponseBorrowedStr = responseBorrowed.toString();
                    int startBorrowedBookHtml = reponseBorrowedStr.indexOf("<!-- filename: bor-loan-body -->");
                    int endBorrowedBookHtml = reponseBorrowedStr.indexOf("<!-- filename: bor-loan-tail -->");
//                            Log.d("swkDebug", String.valueOf(startBorrowedBookHtml));
//                            Log.d("swkDebug", String.valueOf(endBorrowedBookHtml));
                    String borrowedBooksHtml = reponseBorrowedStr.substring(startBorrowedBookHtml, endBorrowedBookHtml);
//                            Log.d("swkDebug", borrowedBooksHtml);
                    String[] borrowedBooksHtmls = borrowedBooksHtml.split("<!-- filename: bor-loan-body -->");
                    for (String s:borrowedBooksHtmls
                            ) {
                        if (s.equals("")) {
                            continue;
                        }
                        s = s.replaceAll("^[\"()]+", "").replaceAll("[\"()]+$", "");
                        org.jsoup.nodes.Document borrowedDoc = Jsoup.parse(s);
                        Elements eChild = borrowedDoc.children();
                        Elements eTd = eChild.select("td");
                        String[] strTempArrary = eChild.text().split("\n");
                        for (int i = 0; i < strTempArrary.length; i++) {
                            String[] strInnerArr = strTempArrary[i].split(" ");
                            for (int j = 0; j < strInnerArr.length; j++) {
                                Log.d("swkDebug", strInnerArr[j] + " in "+String.valueOf(j));
                            }
                            books.add(new Book(strInnerArr[0], strInnerArr[1], strInnerArr[2], strInnerArr[4], strInnerArr[3]));
//                                    Log.d("swkDebug", String.valueOf(strTempArrary[i]) + " in "+String.valueOf(i));
//                                    Log.d("swkDebug", String.valueOf(i));
                        }

//                                Log.d("swkDebug",s);

                    }
                }
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        } finally {
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Message message = new Message();
        if (aBoolean == true) {
            message.what = MainActivity.getData_success;
            handler.sendMessage(message);
        }
        else{
            message.what = MainActivity.getData_failed;
            handler.sendMessage(message);

        }
    }
}