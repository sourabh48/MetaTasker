package com.example.soura.metatasker;


public class Messages {

    private String comments, type,from;
    private long time;


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String comments, String type, long time,String from) {
        this.from = from;
        this.comments = comments;
        this.type = type;
        this.time = time;
    }

    public Messages(){}


    public String getComments()
    {
        return comments;
    }

    public void setComments(String comments)
    {
        this.comments = comments;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}