package com.donatehub.DonatedItem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonatedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donated_item_id")
    private Long id;

    private String name;
    private String category;

    @Column(name = "item_condition")
    private String condition;
    private int quantity;
    private int distributedQuantity = 0;
    private Long donorId;
    private Long driveId;
}
