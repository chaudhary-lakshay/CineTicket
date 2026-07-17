package in.lakshay.integration;

import in.lakshay.dto.ReservationDTO;
import in.lakshay.entity.Movie;
import in.lakshay.entity.Seat;
import in.lakshay.entity.Showtime;
import in.lakshay.entity.Theater;
import in.lakshay.entity.User;
import in.lakshay.repo.MovieRepository;
import in.lakshay.repo.SeatRepository;
import in.lakshay.repo.ShowtimeRepository;
import in.lakshay.repo.TheaterRepository;
import in.lakshay.repo.UserRepository;
import in.lakshay.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the crown jewel of this codebase: concurrent seat
 * booking protected by a pessimistic write lock (SeatRepository
 * .findByIdInAndShowtimeIdWithLock). Runs against a real MySQL via
 * Testcontainers; schema.sql/data.sql initialize the container database.
 */
@SpringBootTest(properties = {
        // application.properties has no JWT_SECRET fallback (by design); tests provide one
        "jwt.secret=YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ=="
})
@Testcontainers
class SeatBookingIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    private User user; // seeded by data.sql (user/password)
    private Showtime showtime;
    private List<Seat> seats;

    @BeforeEach
    void setUp() {
        user = userRepository.findByUserName("user")
                .orElseThrow(() -> new IllegalStateException("data.sql should seed the 'user' account"));

        Movie movie = new Movie();
        movie.setTitle("Integration Test Movie");
        movie.setGenre("Test");
        movie.setReleaseYear(2026);
        movie.setDescription("created by SeatBookingIntegrationTest");
        movie = movieRepository.save(movie);

        Theater theater = new Theater();
        theater.setName("Integration Test Theater");
        theater.setLocation("Test City");
        theater.setCapacity(5);
        theater = theaterRepository.save(theater);

        showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater(theater);
        showtime.setShowDate(LocalDate.now().plusDays(1)); // future, so booking is allowed
        showtime.setShowTime(LocalTime.NOON);
        showtime.setTotalSeats(5);
        showtime.setAvailableSeats(5);
        showtime.setPrice(10.0);
        showtime = showtimeRepository.save(showtime);

        seats = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Seat seat = new Seat();
            seat.setShowtime(showtime);
            seat.setSeatNumber("T" + i);
            seat.setIsReserved(false);
            seats.add(seatRepository.save(seat));
        }
    }

    @Test
    void bookingASeatReservesItAndDecrementsAvailability() {
        Seat seat = seats.get(0);

        ReservationDTO reservation = reservationService.createReservation(
                user.getUserName(), showtime.getId(), List.of(seat.getId()));

        assertNotNull(reservation);

        Seat reloaded = seatRepository.findById(seat.getId()).orElseThrow();
        assertTrue(reloaded.getIsReserved(), "seat must be marked reserved");

        Showtime reloadedShowtime = showtimeRepository.findById(showtime.getId()).orElseThrow();
        assertEquals(4, reloadedShowtime.getAvailableSeats(), "availability must drop by exactly one");
    }

    @Test
    void concurrentBookingsOfTheSameSeatAllowExactlyOneWinner() throws Exception {
        Seat contestedSeat = seats.get(1);
        int attempts = 4;

        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch startGun = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startGun.await();
                    reservationService.createReservation(
                            user.getUserName(), showtime.getId(), List.of(contestedSeat.getId()));
                    successes.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // losers are expected to fail ("Seats already reserved" or a lock timeout)
                    failures.incrementAndGet();
                }
            }));
        }

        startGun.countDown();
        for (Future<?> f : futures) {
            f.get(60, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(1, successes.get(), "exactly one booking must win the seat");
        assertEquals(attempts - 1, failures.get(), "every other attempt must fail");

        Seat reloaded = seatRepository.findById(contestedSeat.getId()).orElseThrow();
        assertTrue(reloaded.getIsReserved(), "contested seat must end up reserved");

        Showtime reloadedShowtime = showtimeRepository.findById(showtime.getId()).orElseThrow();
        assertEquals(4, reloadedShowtime.getAvailableSeats(),
                "availability must drop by exactly one despite concurrent attempts");
    }
}
