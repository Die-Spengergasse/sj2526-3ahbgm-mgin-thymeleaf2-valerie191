package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Gender;
import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping("/list")
    public String patients(Model model) {
        try {
            model.addAttribute("patients", patientRepository.findAll());
            return "patlist";
        } catch (DataAccessException e) {
            model.addAttribute("error", "Die Datenbank ist momentan nicht erreichbar. Bitte prüfen Sie, ob MySQL läuft.");
            return "patlist";
        }
    }

    @GetMapping("/add")
    public String addPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("genders", Gender.values());
        return "add_patient";
    }

    @PostMapping("/add")
    public String addPatient(@ModelAttribute("patient") Patient patient, Model model) {
        try {
            if (patient.getBirthDate() != null && patient.getBirthDate().isAfter(LocalDate.now())) {
                model.addAttribute("error", "Das Geburtsdatum darf nicht in der Zukunft liegen.");
                model.addAttribute("genders", Gender.values());
                return "add_patient";
            }

            if (!isValidSocialSecurityNumber(patient.getSocialSecurityNumber())) {
                model.addAttribute("error", "Die eingegebene Sozialversicherungsnummer ist ungültig.");
                model.addAttribute("genders", Gender.values());
                return "add_patient";
            }

            if (patientRepository.existsBySocialSecurityNumber(patient.getSocialSecurityNumber())) {
                model.addAttribute("error", "Ein Patient mit dieser Sozialversicherungsnummer existiert bereits.");
                model.addAttribute("genders", Gender.values());
                return "add_patient";
            }

            patientRepository.save(patient);
            return "redirect:/patient/list";

        } catch (DataAccessException e) {
            model.addAttribute("error", "Die Datenbank ist momentan nicht erreichbar. Bitte prüfen Sie, ob MySQL läuft.");
            model.addAttribute("genders", Gender.values());
            return "add_patient";
        }
    }

    private boolean isValidSocialSecurityNumber(String ssn) {
        if (ssn == null) {
            return false;
        }

        String cleaned = ssn.replaceAll("\\s+", "");

        return cleaned.matches("\\d{10}");
    }
}