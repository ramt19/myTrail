package com.wip.major.mytrail;

import java.util.ArrayList;

public class Contact {
    private String name;
    private String number;
    private ArrayList<Contact> contact = new ArrayList<>();

    public Contact() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
