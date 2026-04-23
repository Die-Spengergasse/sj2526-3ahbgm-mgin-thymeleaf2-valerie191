package at.spengergasse.spring_thymeleaf.entities;

import jakarta.persistence.*;

@Entity
public class Device {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String designation;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String roomNumber;

    @Column (nullable = false)
    private boolean available = true;

    public Device() {
    }

    public Device(String designation, String type, String roomNumber) {
        this.designation = designation;
        this.type = type;
        this.roomNumber = roomNumber;
    }

    public Integer getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}