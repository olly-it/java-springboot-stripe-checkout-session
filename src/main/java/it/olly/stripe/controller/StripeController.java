package it.olly.stripe.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // BASE
    @Value("${stripe.pkg.base.priceId}")
    private String basePriceId;
    @Value("${stripe.pkg.base.name}")
    private String baseName;
    @Value("${stripe.pkg.base.price}")
    private String basePrice;

    // PREMIUM
    @Value("${stripe.pkg.premium.priceId}")
    private String premiumPriceId;
    @Value("${stripe.pkg.premium.name}")
    private String premiumName;
    @Value("${stripe.pkg.premium.price}")
    private String premiumPrice;

    @Value("${stripe.redirect.success.url}")
    private String stripeRedirectSuccessUrl;
    @Value("${stripe.redirect.cancel.url}")
    private String stripeRedirectCancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        System.out.println("inited with apikey: " + stripeApiKey.substring(0, 15) + "...");
    }

    @GetMapping("/packages")
    public ResponseEntity<List<Map<String, String>>> getAvailablePackages() {
        System.out.println("GET getAvailablePackages()");
        List<Map<String, String>> packages = new ArrayList<>();

        Map<String, String> base = new HashMap<>();
        base.put("name", baseName);
        base.put("priceId", basePriceId);
        base.put("price", "€" + basePrice);

        Map<String, String> premium = new HashMap<>();
        premium.put("name", premiumName);
        premium.put("priceId", premiumPriceId);
        premium.put("price", "€" + premiumPrice);

        packages.add(base);
        packages.add(premium);

        System.out.println("GET getAvailablePackages -> " + packages);
        return ResponseEntity.ok(packages);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, Object> payload)
            throws StripeException {
        System.out.println("POST createCheckoutSession(" + payload + ")");
        String priceId = (String) payload.get("priceId"); // ricevuto dal frontend

        String myTransactionId = UUID.randomUUID()
                .toString();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeRedirectSuccessUrl)
                .setCancelUrl(stripeRedirectCancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("myTransactionId", myTransactionId)
                        .build())
                .build();

        Session session = Session.create(params);

        System.out.println("POST createCheckoutSession - created session. ID: " + session.getId()
                + ", myTransactionId: " + myTransactionId);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("url", session.getUrl());

        System.out.println("POST createCheckoutSession -> " + responseData);
        return ResponseEntity.ok(responseData);
    }

}
