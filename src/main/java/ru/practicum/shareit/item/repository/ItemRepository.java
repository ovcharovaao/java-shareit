package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwner_Id(Long ownerId, Sort sort);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "  AND (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "    OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%')))")
    List<Item> search(String text);
}
