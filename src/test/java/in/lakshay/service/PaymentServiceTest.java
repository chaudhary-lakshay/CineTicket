package in.lakshay.service;

import com.itextpdf.text.DocumentException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import in.lakshay.dto.CheckoutSessionDTO;
import in.lakshay.entity.Payment;
import in.lakshay.entity.Reservation;
import in.lakshay.entity.User;
import in.lakshay.entity.Showtime;
import in.lakshay.entity.Movie;
import in.lakshay.repo.PaymentRepository;
import in.lakshay.repo.ReservationRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PdfService pdfService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(paymentService, "webhookSecret", "whsec_sample");

        // service enforces owner-or-admin; tests run as the reservation owner
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testHandleCheckoutSessionCompleted() {
        // Setup
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("test@example.com");

        Showtime showtime = new Showtime();
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        showtime.setMovie(movie);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setShowtime(showtime);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setReservation(reservation);

        when(paymentRepository.findByPaymentIntentId("cs_test_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByReservation(any(Reservation.class))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // raw Stripe event payload, as the production webhook path parses it
        String payload = "{\"data\":{\"object\":{\"id\":\"cs_test_123\"}}}";

        ReflectionTestUtils.invokeMethod(paymentService, "handleCheckoutSessionCompleted", payload);

        // Verify
        assertEquals(Payment.PaymentStatus.SUCCEEDED, payment.getStatus());
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void testGetPaymentByReservationIdThrowsExceptionWhenNotFound() {
        // Setup
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(Exception.class, () -> {
            paymentService.getPaymentByReservationId(1L);
        });
    }

    @Test
    void testCreateCheckoutSession() throws Exception, IOException {
        // Setup
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setTotalPrice(25.0);

        User user = new User();
        user.setEmail("test@example.com");
        user.setUserName("testuser");
        reservation.setUser(user);

        Showtime showtime = new Showtime();
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        showtime.setMovie(movie);
        reservation.setShowtime(showtime);
        reservation.setSeats(new ArrayList<>());

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservation(any(Reservation.class))).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Mock the static Stripe Session.create call so no real API request happens
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            Session stripeSession = mock(Session.class);
            when(stripeSession.getId()).thenReturn("cs_test_123");
            when(stripeSession.getUrl()).thenReturn("https://checkout.stripe.com/c/pay/cs_test_123");
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(stripeSession);

            // Test
            CheckoutSessionDTO result = paymentService.createCheckoutSession(
                1L,
                "https://example.com/success",
                "https://example.com/cancel"
            );

            // Verify
            assertNotNull(result);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void testGenerateAndSendReceipt() throws DocumentException, MessagingException, IOException {
        // Setup
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);

        Reservation reservation = new Reservation();
        reservation.setId(1L);

        User user = new User();
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        reservation.setUser(user);

        Showtime showtime = new Showtime();
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        showtime.setMovie(movie);
        reservation.setShowtime(showtime);

        payment.setReservation(reservation);

        // Mock PDF service
        when(pdfService.generateReceipt(any(Payment.class))).thenReturn("test-path/receipt.pdf");

        // Test
        paymentService.generateAndSendReceipt(payment);

        // Verify
        verify(pdfService).generateReceipt(payment);
        verify(emailService).sendReceiptEmail(eq(payment), anyString());
        verify(paymentRepository).save(payment);
    }
}
