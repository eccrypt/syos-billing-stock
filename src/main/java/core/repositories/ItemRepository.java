package core.repositories;

import core.models.Item;
import java.sql.SQLException;
import java.util.List;

public interface ItemRepository {
    Item getItemByCode(String code) throws SQLException;
    List<Item> getAllItems() throws SQLException;
    void addItem(Item item) throws SQLException;
    void updateItem(Item item) throws SQLException;
    void deleteItem(String code) throws SQLException;
}
