package core.services;

import core.models.Item;
import core.repositories.ItemRepository;

import java.sql.SQLException;
import java.util.List;

public class ItemService {
    private final ItemRepository itemRepo;

    public ItemService(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public Item getItemByCode(String code) throws SQLException {
        return itemRepo.getItemByCode(code);
    }

    public List<Item> getAllItems() throws SQLException {
        return itemRepo.getAllItems();
    }

    public void addItem(Item item) throws SQLException {
        itemRepo.addItem(item);
    }

    public void updateItem(Item item) throws SQLException {
        itemRepo.updateItem(item);
    }

    public void updateItemName(String code, String newName) throws SQLException {
        Item item = itemRepo.getItemByCode(code);
        if (item != null) {
            Item updated = new Item(item.getCode(), newName, item.getPrice());
            itemRepo.updateItem(updated);
        }
    }

    public void updateItemPrice(String code, double newPrice) throws SQLException {
        Item item = itemRepo.getItemByCode(code);
        if (item != null) {
            Item updated = new Item(item.getCode(), item.getName(), newPrice);
            itemRepo.updateItem(updated);
        }
    }

    public void deleteItem(String code) throws SQLException {
        itemRepo.deleteItem(code);
    }



}
