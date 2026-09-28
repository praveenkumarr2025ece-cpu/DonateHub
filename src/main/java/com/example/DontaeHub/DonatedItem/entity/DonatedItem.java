package com.example.DontaeHub.DonatedItem.entity;

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
    private Long donatedItemId;

    private String name;

    private String category;

    private String itemCondition;

    private int quantity;

    private Long donorId;

    private Long driveId;
}