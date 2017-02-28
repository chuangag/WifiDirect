package com.example.d4561.wifidirect;

/**
 * Created by d4561 on 2017/2/28.
 */

public class Info {
    public long id;
    public String timeOfMessage;
    public String sender;
    public String receiver;
    public Info(){}
    public Info(String time,String send, String receive){
        timeOfMessage=time;
        sender=send;
        receiver=receive;
    }
    public Info(long i,String time,String send, String receive){
        id=i;
        timeOfMessage=time;
        sender=send;
        receiver=receive;
    }
    public String getTimeOfMessage(){return timeOfMessage;}
    public String getSender(){return sender;}
    public String getReceiver(){return receiver;}
    public long getId(){return id;}
    public void setId(long i){id=i;}
    public void setTimeOfMessage(String msg){timeOfMessage=msg;}
    public void setSender(String s){sender=s;}
    public void setReceiver(String r){receiver=r;}
}
