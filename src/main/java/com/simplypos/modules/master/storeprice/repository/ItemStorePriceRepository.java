package com.simplypos.modules.master.storeprice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.master.storeprice.entity.ItemStorePrice;

public interface ItemStorePriceRepository extends JpaRepository<ItemStorePrice, Long> {

    Optional<ItemStorePrice> findByItemIdAndStoreId(Long itemId, Long storeId);

    List<ItemStorePrice> findByStoreId(Long storeId);

    List<ItemStorePrice> findAllByOrderByStoreIdAscItemIdAsc();

    boolean existsByItemIdAndStoreId(Long itemId, Long storeId);

    /**
     * Cek apakah ada record lain (selain id) yang punya kombinasi item+store sama.
     * Digunakan saat edit untuk mencegah duplikat.
     */
    boolean existsByItemIdAndStoreIdAndIdNot(Long itemId, Long storeId, Long id);
}