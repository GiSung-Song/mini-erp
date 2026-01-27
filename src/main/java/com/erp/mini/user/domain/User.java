package com.erp.mini.user.domain;

import com.erp.mini.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uq_users_employee", columnNames = {"employee_number"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 50, name = "employee_number")
    private String employeeNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private User(String name, String employeeNumber, String password) {
        this.name = name;
        this.employeeNumber = employeeNumber;
        this.password = password;
        this.status = UserStatus.ACTIVE;
    }

    public static User createUser(String name, String employeeNumber, String password) {
        return new User(name, employeeNumber, password);
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
