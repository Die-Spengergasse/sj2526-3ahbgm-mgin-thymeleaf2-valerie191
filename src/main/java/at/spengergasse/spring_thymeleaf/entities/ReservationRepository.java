package at.spengergasse.spring_thymeleaf.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByDeviceOrderByStartTimeAsc(Device device);

    List<Reservation> findAllByOrderByStartTimeAsc();

    boolean existsByDeviceAndStartTimeLessThanAndEndTimeGreaterThan(
            Device device,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    boolean existsByPatientAndStartTimeLessThanAndEndTimeGreaterThan(
            Patient patient,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}