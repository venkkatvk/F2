package com.flashsale.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "product_inventory")
public class InventoryEntity {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private String productId;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Version
    @Column(nullable = false)
    private long version;

    public InventoryEntity() {}

    public InventoryEntity(String productId, int availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
