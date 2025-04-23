package it.olly.stripe.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestHeader("Stripe-Signature") String sigHeader,
            @RequestBody String payload) {
        System.out.println("handleStripeWebhook. H: " + sigHeader + ", P: " + payload);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("handleStripeWebhook - invalid signature");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        }

        // event json
        String jsonStr = event.getDataObjectDeserializer()
                .getRawJson();

        String myTransactionId = null;
        try {
            // try to get myTranscationId for every type of events
            myTransactionId = event.getRawJsonObject()
                    .get("data")
                    .getAsJsonObject()
                    .get("object")
                    .getAsJsonObject()
                    .get("metadata")
                    .getAsJsonObject()
                    .get("myTransactionId")
                    .getAsString();
        } catch (Exception e) {
            // System.out.println("myTransactionId not found on object");
        }

        System.out.println("handleStripeWebhook - event type is: " + event.getType() + ", myTransactionId: "
                + myTransactionId);

        switch (event.getType()) {
            case "checkout.session.completed":
                // NOTE: for many events i have metadata with myTransactionId.
                // for this event metadata is empty.

                // Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                // returns always null.. Version differences?

                Session session = StripeObject.deserializeStripeObject(jsonStr, Session.class,
                                                                       ApiResource.getGlobalResponseGetter());
                if (session != null) {
                    System.out.println("handleStripeWebhook - payment completed. sessionId: " + session.getId()
                            + ", amount: " + session.getAmountTotal());
                } else {
                    System.out.println("handleStripeWebhook - payment completed - session not found in object");
                }
                break;
            // case "charge.failed":
            // System.out.println("charge failed");
            // break;
            default:
                // System.out.println("unmanaged event: " + payload);
                break;
        }

        return ResponseEntity.ok("");
    }
}
