# Java Springboot MVC stripe testing app (working for checkout-sessions)

## Config
stripe.api.key=your_stripe_secret_key_here (from stripe's home, "for developers")
stripe.webhook.secret=your_webhook_signing_secret_here

stripe.pkg.base.priceId=your_product_price_id_1
stripe.pkg.base.name=Base
stripe.pkg.base.price=10

stripe.pkg.premium.priceId=your_product_price_id_2
stripe.pkg.premium.name=Premium
stripe.pkg.premium.price=20

## Webhooks
To use webhooks:

- Go to Stripe Dashboard → Developers → Webhooks (Developers section can be in the footer)
- Add an endpoint (your server URL + /api/stripe/webhook)
- Get the signing secret and add it to your properties file

## Products
Create products and prices in your Stripe dashboard:

- Go to Stripe Dashboard → Products
- Create products with recurring prices
- Update the price IDs in your properties file

## Testing
[https://docs.stripe.com/testing](https://docs.stripe.com/testing)  
  
    **WORKING CARD**  
    Mastercard (credit):  5555 5555 5555 4444  
    CVC:                  Any 3 digits  
    Exp. Date:            Any future date  
    
    Visa (debit):         4000 0566 5566 5556  
    CVC:                  Any 3 digits  
    Exp. Date:            Any future date  
      
      
    **ERROR CARD**  
    Insufficient funds decline:         4000 0000 0000 9995  
    Always blocked (Fraud prevention):  4100 0000 0000 0019  
    CVC check fails:                    4000 0000 0000 0101  

