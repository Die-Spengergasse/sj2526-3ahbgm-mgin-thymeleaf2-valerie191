package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.BodyRegion;
import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import at.spengergasse.spring_thymeleaf.entities.Reservation;
import at.spengergasse.spring_thymeleaf.entities.ReservationRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;

    public ReservationController(ReservationRepository reservationRepository,
                                 PatientRepository patientRepository,
                                 DeviceRepository deviceRepository) {
        this.reservationRepository = reservationRepository;
        this.patientRepository = patientRepository;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/add")
    public String addReservationForm(Model model) {
        try {
            model.addAttribute("reservation", new Reservation());
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("regions", BodyRegion.values());
            return "add_reservation";
        } catch (DataAccessException e) {
            model.addAttribute("error", "Die Datenbank ist momentan nicht erreichbar. Bitte prüfen Sie, ob MySQL läuft.");
            return "add_reservation";
        }
    }

    @PostMapping("/add")
    public String addReservation(@RequestParam Integer patientId,
                                 @RequestParam Integer deviceId,
                                 @RequestParam String startTime,
                                 @RequestParam String endTime,
                                 @RequestParam BodyRegion bodyRegion,
                                 @RequestParam(required = false) String comment,
                                 Model model) {

        try {
            Patient patient = patientRepository.findById(patientId).orElse(null);
            Device device = deviceRepository.findById(deviceId).orElse(null);

            if (patient == null || device == null) {
                model.addAttribute("error", "Patient oder Gerät wurde nicht gefunden.");
                model.addAttribute("reservation", new Reservation());
                model.addAttribute("patients", patientRepository.findAll());
                model.addAttribute("devices", deviceRepository.findAll());
                model.addAttribute("regions", BodyRegion.values());
                return "add_reservation";
            }

            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);

            if (start.isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "Eine Reservierung darf nicht in der Vergangenheit liegen.");
                model.addAttribute("reservation", new Reservation());
                model.addAttribute("patients", patientRepository.findAll());
                model.addAttribute("devices", deviceRepository.findAll());
                model.addAttribute("regions", BodyRegion.values());
                return "add_reservation";
            }

            if (!end.isAfter(start)) {
                model.addAttribute("error", "Die Endzeit muss nach der Startzeit liegen.");
                model.addAttribute("reservation", new Reservation());
                model.addAttribute("patients", patientRepository.findAll());
                model.addAttribute("devices", deviceRepository.findAll());
                model.addAttribute("regions", BodyRegion.values());
                return "add_reservation";
            }

            boolean deviceConflict = reservationRepository
                    .existsByDeviceAndStartTimeLessThanAndEndTimeGreaterThan(device, end, start);

            if (deviceConflict) {
                model.addAttribute("error", "Für dieses Gerät existiert in diesem Zeitraum bereits eine Reservierung.");
                model.addAttribute("reservation", new Reservation());
                model.addAttribute("patients", patientRepository.findAll());
                model.addAttribute("devices", deviceRepository.findAll());
                model.addAttribute("regions", BodyRegion.values());
                return "add_reservation";
            }

            boolean patientConflict = reservationRepository
                    .existsByPatientAndStartTimeLessThanAndEndTimeGreaterThan(patient, end, start);

            if (patientConflict) {
                model.addAttribute("error", "Dieser Patient hat in diesem Zeitraum bereits eine Reservierung.");
                model.addAttribute("reservation", new Reservation());
                model.addAttribute("patients", patientRepository.findAll());
                model.addAttribute("devices", deviceRepository.findAll());
                model.addAttribute("regions", BodyRegion.values());
                return "add_reservation";
            }

            Reservation reservation = new Reservation();
            reservation.setPatient(patient);
            reservation.setDevice(device);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setBodyRegion(bodyRegion);
            reservation.setComment(comment);

            reservationRepository.save(reservation);

            return "redirect:/reservation/list?deviceId=" + deviceId;

        } catch (DataAccessException e) {
            model.addAttribute("error", "Die Datenbank ist momentan nicht erreichbar. Bitte prüfen Sie, ob MySQL läuft.");
            model.addAttribute("reservation", new Reservation());
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("regions", BodyRegion.values());
            return "add_reservation";
        }
    }

    @GetMapping("/list")
    public String reservationList(@RequestParam(required = false) Integer deviceId, Model model) {
        try {
            var devices = deviceRepository.findAll();
            model.addAttribute("devices", devices);

            if (deviceId != null) {
                Device selectedDevice = deviceRepository.findById(deviceId).orElse(null);
                model.addAttribute("selectedDevice", selectedDevice);

                if (selectedDevice != null) {
                    model.addAttribute("reservations", reservationRepository.findByDeviceOrderByStartTimeAsc(selectedDevice));
                } else {
                    model.addAttribute("reservations", reservationRepository.findAllByOrderByStartTimeAsc());
                }
            } else {
                model.addAttribute("selectedDevice", null);
                model.addAttribute("reservations", reservationRepository.findAllByOrderByStartTimeAsc());
            }

            return "reservation_list";
        } catch (DataAccessException e) {
            model.addAttribute("error", "Die Datenbank ist momentan nicht erreichbar. Bitte prüfen Sie, ob MySQL läuft.");
            model.addAttribute("reservations", java.util.Collections.emptyList());
            model.addAttribute("devices", java.util.Collections.emptyList());
            return "reservation_list";
        }
    }
}

//katze