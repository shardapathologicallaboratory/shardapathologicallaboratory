package com.lcms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String qualification;

    @Column(length = 200)
    private String clinicOrHospital;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Doctor() {}

    public Doctor(String name, String qualification, String clinicOrHospital, String phone, String email) {
        this.name = name;
        this.qualification = qualification;
        this.clinicOrHospital = clinicOrHospital;
        this.phone = phone;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getClinicOrHospital() {
        return clinicOrHospital;
    }

    public void setClinicOrHospital(String clinicOrHospital) {
        this.clinicOrHospital = clinicOrHospital;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
