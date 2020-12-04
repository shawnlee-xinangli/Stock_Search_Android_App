package com.example.myapplication;

import java.util.List;

public class Section {

    private String sectionName;
    private double netWorth;
    private List<StockItem> sectionItems;

    public Section(String sectionName, double netWorth,List<StockItem> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
        this.netWorth = netWorth;
    }

    public String getSectionName() {
        return sectionName;
    }

    public List<StockItem> getSectionItems() {
        return sectionItems;
    }

    public double getNetWorth(){return netWorth;}

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setSectionItems(List<StockItem> sectionItems) {
        this.sectionItems = sectionItems;
    }

    @Override
    public String toString() {
        return "Section{" +
                "sectionName='" + sectionName + '\'' +
                ", netWorth=" + netWorth +
                ", sectionItems=" + sectionItems +
                '}';
    }
}
