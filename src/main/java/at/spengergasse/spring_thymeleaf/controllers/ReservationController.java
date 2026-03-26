package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("regions", BodyRegion.values());
        return "add_reservation";
    }

    @PostMapping("/add")
    public String addReservation(@RequestParam Integer patientId,
                                 @RequestParam Integer deviceId,
                                 @RequestParam String startTime,
                                 @RequestParam String endTime,
                                 @RequestParam BodyRegion bodyRegion,
                                 @RequestParam(required = false) String comment,
                                 Model model) {

        Patient patient = patientRepository.findById(patientId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);

        if (patient == null || device == null) {
            model.addAttribute("error", "Patient oder Gerät wurde nicht gefunden.");
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("regions", BodyRegion.values());
            model.addAttribute("reservation", new Reservation());
            return "add_reservation";
        }

        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);

        if (!end.isAfter(start)) {
            model.addAttribute("error", "Die Endzeit muss nach der Startzeit liegen.");
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("regions", BodyRegion.values());
            model.addAttribute("reservation", new Reservation());
            return "add_reservation";
        }

        boolean conflict = reservationRepository
                .existsByDeviceAndStartTimeLessThanAndEndTimeGreaterThan(device, end, start);

        if (conflict) {
            model.addAttribute("error", "Für dieses Gerät existiert in diesem Zeitraum bereits eine Reservierung.");
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("regions", BodyRegion.values());
            model.addAttribute("reservation", new Reservation());
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
    }

    @GetMapping("/list")
    public String reservationList(@RequestParam(required = false) Integer deviceId, Model model) {
        var devices = deviceRepository.findAll();
        model.addAttribute("devices", devices);

        Device device = null;

        if (deviceId != null) {
            device = deviceRepository.findById(deviceId).orElse(null);
        } else if (!devices.isEmpty()) {
            device = devices.get(0);
        }

        model.addAttribute("selectedDevice", device);

        if (device != null) {
            model.addAttribute("reservations", reservationRepository.findByDeviceOrderByStartTimeAsc(device));
        }

        return "reservation_list";
    }
}