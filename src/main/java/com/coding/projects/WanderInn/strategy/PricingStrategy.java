package com.coding.projects.WanderInn.strategy;

import com.coding.projects.WanderInn.entity.Inventory;

import java.math.BigDecimal;
public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
