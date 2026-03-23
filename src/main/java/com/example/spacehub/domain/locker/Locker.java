package com.example.spacehub.domain.locker;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lockers")
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String lockerNumber;

    @Column(nullable = false, length = 50)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerStatus status;

    public Locker(String lockerNumber, String location) {
        this.lockerNumber = lockerNumber;
        this.location = location;
        this.status = LockerStatus.AVAILABLE;
    }

    public void reserve() {
        this.status = LockerStatus.RESERVED;
    }

    public void makeAvailable() {
        this.status = LockerStatus.AVAILABLE;
    }

    public void markBroken() {
        this.status = LockerStatus.BROKEN;
    }

    public void disable() {
        this.status = LockerStatus.DISABLED;
    }

    public boolean isAvailable() {
        return this.status == LockerStatus.AVAILABLE;
    }

    public boolean isBrokenOrDisabled() {
        return this.status == LockerStatus.BROKEN || this.status == LockerStatus.DISABLED;
    }
}