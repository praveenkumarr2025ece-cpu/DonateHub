package com.donatehub.DonatedItem.Repository;

import com.donatehub.DonatedItem.entity.DonatedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonatedItemRepository extends JpaRepository<DonatedItem, Long> {

    List<DonatedItem> findByDriveId(Long driveId);

    List<DonatedItem> findByDonorId(Long donorId);
}
