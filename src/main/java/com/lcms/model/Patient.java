package com.lcms.model;

import com.lcms.model.enums.Gender;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_patients", indexes = {
    @Index(name = "idx_patient_uhid", columnList = "uhid", unique = true),
    @Index(name = "idx_patient_phone", columnList = "phone"),
    @Index(name = "idx_patient_name", columnList = "fullName")
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String uhid;

    @Column(length = 20)
    private String salutation = "Mr.";

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer ageYears = 0;

    private Integer ageMonths = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender = Gender.MALE;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 10)
    private String bloodGroup;

    private boolean locked = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Patient() {}

    public Patient(String uhid, String salutation, String fullName, Integer ageYears, Integer ageMonths, Gender gender, String phone, String address) {
        this.uhid = uhid;
        this.salutation = salutation;
        this.fullName = fullName;
        this.ageYears = ageYears;
        this.ageMonths = ageMonths;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.locked = true;
    }

    public String getFormattedAgeAndSex() {
        StringBuilder sb = new StringBuilder();
        if (ageYears != null && ageYears > 0) {
            sb.append(ageYears).append(" Yrs. ");
        }
        if (ageMonths != null && ageMonths > 0) {
            sb.append(ageMonths).append(" Mths. ");
        }
        if (sb.length() == 0) {
            sb.append("0 Yrs. ");
        }
        sb.append("/ ").append(gender != null ? gender.getDisplayName() : "Unknown");
        return sb.toString().trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(Integer ageYears) {
        this.ageYears = ageYears;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
