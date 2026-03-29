package com.skywind.fluxis.action.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItem {
    private UUID id;
    private UUID sellerId;
    private String sellerName;
    private ItemStack itemStack;
    private double price;
    private long listedAt;
}
