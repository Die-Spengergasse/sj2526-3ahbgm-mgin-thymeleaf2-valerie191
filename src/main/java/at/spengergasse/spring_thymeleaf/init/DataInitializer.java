package at.spengergasse.spring_thymeleaf.init;

import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DeviceRepository deviceRepository;

    public DataInitializer(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void run(String... args) {
        if (deviceRepository.count() == 0) {
            deviceRepository.save(new Device("MR-01", "MR", "R101"));
            deviceRepository.save(new Device("CT-01", "CT", "R102"));
            deviceRepository.save(new Device("RX-01", "Röntgen", "R103"));
            deviceRepository.save(new Device("US-01", "Ultraschall", "R104"));
        }
    }
}