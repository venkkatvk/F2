package com.flashsale.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE product_inventory
            SET available_stock = available_stock - :quantity
            WHERE product_id = :productId AND available_stock >= :quantity
            """, nativeQuery = true)
    int decrementStock(@Param("productId") String productId, @Param("quantity") int quantity);
}
