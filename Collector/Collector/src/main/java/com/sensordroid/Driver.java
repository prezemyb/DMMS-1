package com.sensordroid;

/**
 * Created by sveinpg on 28.01.16.
 */
public class Driver {
    private String name;
    private int id;
    private boolean selected = false;

    public Driver(String name,int id, boolean selected) {
        this.name = name;
        this.id = id;
        this.selected = selected;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected(){
        return this.selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
}
