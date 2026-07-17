package in.lakshay.controller;

import com.stripe.exception.StripeException;
import in.lakshay.dto.ApiResponse;
import in.lakshay.dto.PaymentDTO;
import in.lakshay.dto.CheckoutSessionDTO;
import in.lakshay.dto.PaymentRequest;
import in.lakshay.service.PaymentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments") // base path for payment endpoints
@Slf4j // logging
@Tag(name = "Payment", description = "Payment management APIs") // swagger docs
public class PaymentController {

    @Autowired // TODO: switch to constructor injection
    private PaymentService paymentService; // handles payment processing

    @Autowired
    private MessageSource messageSource; // i18n

    @PostMapping("/create-checkout-session") // create stripe checkout session
    @PreAuthorize("isAuthenticated()") // must be logged in
    @RateLimiter(name = "basic") // prevent abuse
    @Operation(summary = "Create checkout session", description = "Creates a Stripe checkout session for a reservation")
    public ResponseEntity<ApiResponse<CheckoutSessionDTO>> createCheckoutSession(
            @Valid @RequestBody PaymentRequest paymentRequest) {

        log.info("Creating checkout session for reservation: {}", paymentRequest.getReservationId());

        try {
            // Client should provide complete URLs for success and cancel redirects
            // these are where stripe will redirect after payment
            String successUrl = paymentRequest.getSuccessUrl();
            String cancelUrl = paymentRequest.getCancelUrl();

            // Validate URLs - both required
            if (successUrl == null || cancelUrl == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        false,
                        "Success and cancel URLs are required", // simple error msg
                        null
                ));
            }

            // create the checkout session with stripe
            CheckoutSessionDTO checkoutSessionDTO = paymentService.createCheckoutSession(
                    paymentRequest.getReservationId(), successUrl, cancelUrl);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    messageSource.getMessage("payment.session.created", null, LocaleContextHolder.getLocale()),
                    checkoutSessionDTO
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (StripeException e) {
            // something went wrong with stripe
            log.error("Stripe error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                            false,
                            "Stripe error: " + e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            // something else went wrong
            log.error("Error creating checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            messageSource.getMessage("payment.session.error", null, LocaleContextHolder.getLocale()),
                            null
                    ));
        }
    }

    @GetMapping("/reservation/{reservationId}") // get payment for a reservation
    @PreAuthorize("isAuthenticated()") // must be logged in
    @RateLimiter(name = "basic")
    @Operation(summary = "Get payment by reservation", description = "Returns payment details for a reservation")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByReservation(@PathVariable Long reservationId) {
        log.info("Getting payment for reservation: {}", reservationId);

        try {
            // ownership is enforced in the service layer (owner or admin only)

            PaymentDTO paymentDTO = paymentService.getPaymentByReservationId(reservationId);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    messageSource.getMessage("payment.retrieved", null, LocaleContextHolder.getLocale()),
                    paymentDTO
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            // something went wrong
            log.error("Error retrieving payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null
                    ));
        }
    }

    @PostMapping("/webhook") // stripe webhook endpoint
    @Operation(summary = "Stripe webhook", description = "Handles Stripe webhook events")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request, @RequestBody String payload) {
        log.info("Received Stripe webhook");

        // get signature header for verification
        String sigHeader = request.getHeader("Stripe-Signature");
        log.debug("Stripe-Signature header: {}", sigHeader);

        // signature is required for security
        if (sigHeader == null) {
            log.error("Missing Stripe-Signature header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
        }

        try {
            // process the webhook - this will update payment status
            // and send email receipt if payment successful
            paymentService.handleWebhookEvent(payload, sigHeader);
            log.info("Webhook processed successfully");
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (StripeException e) {
            // stripe validation failed
            log.error("Stripe webhook error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        } catch (Exception e) {
            // something else went wrong
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }
} // end of PaymentController
