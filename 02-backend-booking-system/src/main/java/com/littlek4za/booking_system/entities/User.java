package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "guest", nullable = false)
    private Boolean guest;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookingList = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roleSet = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Event> eventSet = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Invitation> invitationSet = new HashSet<>();

    @OneToMany(mappedBy ="user", fetch = FetchType.LAZY)
    private Set<InvitationUsage> invitationUsages = new HashSet<>();

    protected User() {
    }

    public static User createRegistered(String username, String password, String email, String firstName,
            String lastName) {
        User user = new User();
        user.guest = false;
        user.username = username;
        user.password = password;
        user.email = email;
        user.firstName = firstName;
        user.lastName = lastName;
        return user;
    }

    public static User createGuest(String email, String firstName, String lastName) {
        User user = new User();
        user.guest = true;
        user.email = email;
        user.firstName = firstName;
        user.lastName = lastName;
        return user;
    }

    @AssertTrue(message = "username and password must be provided when guest is false")
    private boolean isCredentialValid() {
        if (!Boolean.TRUE.equals(guest)) {
            return true; // username password not required
        }
        return username != null && password != null;
    }

    public void addRole(Role role) {
        roleSet.add(role);
        role.getUserSet().add(this);
    }

    public void removeRole(Role role) {
        roleSet.remove(role);
        role.getUserSet().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
