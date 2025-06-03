package hr.java.web.prosport.service;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import hr.java.web.prosport.dto.CartDto;
import hr.java.web.prosport.dto.CartItemDto;
import hr.java.web.prosport.exception.PayPalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private static final String CURRENCY_CODE = "EUR";
    private static final String BRAND_NAME = "ProSport";
    private static final String LANDING_PAGE = "BILLING";
    private static final String USER_ACTION = "PAY_NOW";
    private static final String CAPTURE_INTENT = "CAPTURE";
    private static final String CATEGORY_PHYSICAL_GOODS = "PHYSICAL_GOODS";
    private static final String ORDER_DESCRIPTION_PREFIX = "ProSport narudžba #";
    private static final String ZERO_AMOUNT = "0.00";
    private static final String CREATE_ORDER_ERROR = "Failed to create PayPal order: ";
    private static final String CAPTURE_ORDER_ERROR = "Failed to capture PayPal order: ";
    private static final String GET_ORDER_ERROR = "Failed to get PayPal order: ";

    private final PayPalHttpClient payPalHttpClient;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    public String createOrder(CartDto cart, String orderNumber) {
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
            throw new PayPalException(CREATE_ORDER_ERROR + e.getMessage(), e);
        }
    }

    public CaptureResult captureOrder(String paypalOrderId) {
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
            throw new PayPalException(CAPTURE_ORDER_ERROR + e.getMessage(), e);
        }
    }

    public Order getOrder(String paypalOrderId) {
        log.info("Getting PayPal order details: {}", paypalOrderId);

        OrdersGetRequest request = new OrdersGetRequest(paypalOrderId);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();

        } catch (Exception e) {
            log.error("Error getting PayPal order {}: {}", paypalOrderId, e.getMessage(), e);
            throw new PayPalException(GET_ORDER_ERROR + e.getMessage(), e);
        }
    }

    private OrderRequest buildOrderRequest(CartDto cart, String orderNumber) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent(CAPTURE_INTENT);

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(BRAND_NAME)
                .landingPage(LANDING_PAGE)
                .userAction(USER_ACTION)
                .returnUrl(successUrl)
                .cancelUrl(cancelUrl);
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnits = List.of(
                new PurchaseUnitRequest()
                        .referenceId(orderNumber)
                        .description(ORDER_DESCRIPTION_PREFIX + orderNumber)
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
                .currencyCode(CURRENCY_CODE)
                .value(totalAmount)
                .amountBreakdown(
                        new AmountBreakdown()
                                .itemTotal(new Money().currencyCode(CURRENCY_CODE).value(totalAmount))
                                .shipping(new Money().currencyCode(CURRENCY_CODE).value(ZERO_AMOUNT))
                                .taxTotal(new Money().currencyCode(CURRENCY_CODE).value(ZERO_AMOUNT))
                );
    }

    private List<Item> buildItems(CartDto cart) {
        return cart.getItems().stream()
                .map(this::buildItem)
                .toList();
    }

    private Item buildItem(CartItemDto cartItem) {
        return new Item()
                .name(cartItem.getProductName())
                .description(cartItem.getProductBrand() + " - " + cartItem.getCategoryName())
                .sku(cartItem.getProductId().toString())
                .unitAmount(new Money()
                        .currencyCode(CURRENCY_CODE)
                        .value(cartItem.getPriceAtTime().toString()))
                .quantity(cartItem.getQuantity().toString())
                .category(CATEGORY_PHYSICAL_GOODS);
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