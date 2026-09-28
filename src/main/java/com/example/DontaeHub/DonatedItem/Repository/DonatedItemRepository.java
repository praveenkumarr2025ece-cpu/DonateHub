package com.example.DontaeHub.DonatedItem.Repository;

import com.example.DontaeHub.DonatedItem.entity.DonatedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonatedItemRepository
        extends JpaRepository<DonatedItem, Long> {

}