package net.twistedmc.shield.Util;

public class Utils {
    private int years;
    private int months;
    private int days;
    private int hours;
    private int minutes;
    private int seconds;

    public Utils(long epochSeconds) {
        int number = (int) epochSeconds;

        int index = 0;
        while (number != 0) {
            int result;
            switch (index) {
                case 0:
                    result = number / 29030400;
                    number %= 29030400;
                    years = result;
                    break;
                case 1:
                    result = number / 2419200;
                    number %= 2419200;
                    months = result;

                    break;
                case 2:
                    result = number / 86400;
                    number %= 86400;
                    days = result;

                    break;
                case 3:
                    result = number / 3600;
                    number %= 3600;
                    hours = result;

                    break;
                case 4:
                    result = number / 60;
                    number %= 60;
                    minutes = result;

                    break;
                case 5:
                    result = number;
                    number = 0;
                    seconds = result;
                    break;
            }
            index++;
        }
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        String yearplaceholder = "";
        String dayplaceholder = "";
        String hourplaceholder = "";
        String minuteplaceholder = "";
        String secondplaceholder = "";
        if(years != 0 && years > 1)
            yearplaceholder = years + " years, ";
        if(days != 0 && days > 1)
            dayplaceholder = days + " days, ";
        if(hours != 0 && hours > 1)
            hourplaceholder = hours + " hours, ";
        if(minutes != 0 && minutes > 1)
            minuteplaceholder = minutes + " minutes, ";
        if(seconds != 0 && seconds > 1)
            secondplaceholder = seconds + " seconds";
        if(years != 0 && years < 2)
            yearplaceholder = years + " year, ";
        if(days != 0 && days < 2)
            dayplaceholder = days + " day, ";
        if(hours != 0 && hours < 2)
            hourplaceholder = hours + " hour, ";
        if(minutes != 0 && minutes < 2)
            minuteplaceholder = minutes + " minute, ";
        if(seconds != 0 && seconds < 2)
            secondplaceholder = seconds + " second";
        return yearplaceholder + dayplaceholder + hourplaceholder + minuteplaceholder + secondplaceholder;
    }
}
