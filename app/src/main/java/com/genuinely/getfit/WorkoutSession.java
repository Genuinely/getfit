package com.genuinely.getfit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutSession
{
    long seconds;
    long minutes;
    long hours;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public WorkoutSession(){
        reset();
    }

    public void reset(){
        seconds = 0;
        minutes = 0;
        hours = 0;
    }

    public void set(String start, String end)
    {
        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            set(startDate, endDate);
        }
        catch (Exception e){}
    }

    public void set(Date start, Date end)
    {
        set((end.getTime() - start.getTime()) / 1000);
    }

    public void set(long time)
    {
        hours = time / 3600;
        time = time % 3600;
        minutes = time / 60;
        time = time % 60;
        seconds = time;
    }

    public void inc()
    {
        seconds++;
        if (seconds >= 60){
            seconds = seconds - 60;
            minutes++;
            if (minutes >= 60){
                minutes = minutes - 60;
                hours++;
            }
        }
    }

    public String toString(){
        return String.format("%02d", hours) + ":"
                + String.format("%02d", minutes) + ":"
                + String.format("%02d", seconds);
    }

}
