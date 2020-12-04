package com.example.myapplication;

public class StockItem {
    public String ticker;
    public double closePrice;
    public String name;
    public double change;
    public double numberOfShares;

    public StockItem(String ticker, double closePrice, String name, double change, double numberOfShares) {
        this.ticker = ticker;
        this.closePrice = closePrice;
        this.name = name;
        this.change = change;
        this.numberOfShares = numberOfShares;
    }

    @Override
    public String toString() {
        return "stockItem{" +
                "ticker='" + ticker + '\'' +
                ", closePrice=" + closePrice +
                ", name='" + name + '\'' +
                ", change=" + change +
                ", numberOfShares=" + numberOfShares +
                '}';
    }
}
