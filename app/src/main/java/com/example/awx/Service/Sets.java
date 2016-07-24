package com.example.awx.Service;


public class Sets {
    private String addGroupType;
    private boolean noRepeat;
    private int numFilter;

    public String getAddGroupType()
    {
        return this.addGroupType;
    }

    public int getNumFilter()
    {
        return this.numFilter;
    }

    public boolean isNoRepeat()
    {
        return this.noRepeat;
    }

    public void setAddGroupType(String paramString)
    {
        this.addGroupType = paramString;
    }

    public void setNoRepeat(boolean paramBoolean)
    {
        this.noRepeat = paramBoolean;
    }

    public void setNumFilter(int paramInt)
    {
        this.numFilter = paramInt;
    }
}
