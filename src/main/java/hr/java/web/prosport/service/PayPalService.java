package hr.java.web.prosport.service;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.dto.CartItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private final PayPalHttpClient payPalHttpClient;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    public String createOrder(CartDto cart, String orderNumber) throws Exception {
        log.info("Creating PayPal order for cart with {} items, total: {}",
                cart.getTotalItems(), cart.getTotalAmount());

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.requestBody(buildOrderRequest(cart, orderNumber));

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            log.info("PayPal order created successfully with ID: {}", order.id());
            return order.id();

        } catch (Exception e) {
            log.error("Error creating PayPal order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    public CaptureResult captureOrder(String paypalOrderId) throws Exception {
        log.info("Capturing PayPal order: {}", paypalOrderId);

        OrdersCaptureRequest request = new OrdersCaptureRequest(paypalOrderId);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            log.info("PayPal order captured successfully: {}, status: {}",
                    order.id(), order.status());

            String captureId = order.purchaseUnits().get(0).payments().captures().get(0).id();
            return new CaptureResult(order.id(), order.status(), captureId);

        } catch (Exception e) {
            log.error("Error capturing PayPal order {}: {}", paypalOrderId, e.getMessage(), e);
            throw new RuntimeException("Failed to capture PayPal order: " + e.getMessage());
        }
    }

    public Order getOrder(String paypalOrderId) throws Exception {
        log.info("Getting PayPal order details: {}", paypalOrderId);

        OrdersGetRequest request = new OrdersGetRequest(paypalOrderId);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();

        } catch (Exception e) {
            log.error("Error getting PayPal order {}: {}", paypalOrderId, e.getMessage(), e);
            throw new RuntimeException("Failed to get PayPal order: " + e.getMessage());
        }
    }

    private OrderRequest buildOrderRequest(CartDto cart, String orderNumber) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("ProSport")
                .landingPage("BILLING")
                .userAction("PAY_NOW")
                .returnUrl(successUrl)
                .cancelUrl(cancelUrl);
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnits = List.of(
                new PurchaseUnitRequest()
                        .referenceId(orderNumber)
                        .description("ProSport narudžba #" + orderNumber)
                        .customId(orderNumber)
                        .amountWithBreakdown(buildAmount(cart))
                        .items(buildItems(cart))
        );
        orderRequest.purchaseUnits(purchaseUnits);

        return orderRequest;
    }

    private AmountWithBreakdown buildAmount(CartDto cart) {
        String totalAmount = cart.getTotalAmount().toString();

        return new AmountWithBreakdown()
                .currencyCode("EUR")
                .value(totalAmount)
                .amountBreakdown(
                        new AmountBreakdown()
                                .itemTotal(new Money().currencyCode("EUR").value(totalAmount))
                                .shipping(new Money().currencyCode("EUR").value("0.00"))
                                .taxTotal(new Money().currencyCode("EUR").value("0.00"))
                );
    }

    private List<Item> buildItems(CartDto cart) {
        return cart.getItems().stream()
                .map(this::buildItem)
                .collect(Collectors.toList());
    }

    private Item buildItem(CartItemDto cartItem) {
        return new Item()
                .name(cartItem.getProductName())
                .description(cartItem.getProductBrand() + " - " + cartItem.getCategoryName())
                .sku(cartItem.getProductId().toString())
                .unitAmount(new Money()
                        .currencyCode("EUR")
                        .value(cartItem.getPriceAtTime().toString()))
                .quantity(cartItem.getQuantity().toString())
                .category("PHYSICAL_GOODS");
    }

    public static class CaptureResult {
        private final String orderId;
        private final String status;
        private final String captureId;

        public CaptureResult(String orderId, String status, String captureId) {
            this.orderId = orderId;
            this.status = status;
            this.captureId = captureId;
        }

        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public String getCaptureId() { return captureId; }
    }
}