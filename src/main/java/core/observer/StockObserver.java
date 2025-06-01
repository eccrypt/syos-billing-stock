package core.observer;

public interface StockObserver {
    void update(String itemCode, int newQuantity);
}
