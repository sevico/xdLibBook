package org.swkhack.xdlibbook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by swk on 2/21/16.
 */
public class Book {
    private String num;
    private String author;
    private String name;
    private String oriDate;
    private String year;
    private Date retDate;
    private long days;

    public String getNum() {
        return num;
    }

    public Date getRetDate() {
        return retDate;
    }

    public long getDays() {
        return days;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getOriDate() {
        return oriDate;
    }

    public String getYear() {
        return year;
    }

    public Book(String num, String author, String name, String oriDate, String year) {
        this.num = num;
        this.author = author;
        this.name = name;
        this.oriDate = oriDate;
        this.year = year;
        String pattern = "yyyyMMdd";
        SimpleDateFormat dataFormat = new SimpleDateFormat(pattern);
        try {
            this.retDate = dataFormat.parse(oriDate);
            Date current = new Date(System.currentTimeMillis());
            long diff = retDate.getTime() - current.getTime();
            days = diff / (24 * 60 * 60 * 1000);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
