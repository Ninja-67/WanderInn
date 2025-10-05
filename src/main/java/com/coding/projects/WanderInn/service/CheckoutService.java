package com.coding.projects.WanderInn.service;

import com.coding.projects.WanderInn.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);

}
