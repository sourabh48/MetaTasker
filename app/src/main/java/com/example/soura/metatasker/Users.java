package com.example.soura.metatasker;

/**
 * Created by soura on 24-12-2017.
 */

public class Users
{
    public String name;
    public String image;

    public  Users()
    {}

    public Users(String name, String image)
    {
        this.name = name;
        this.image = image;
    }

    public String getName()
    {
            return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

}
