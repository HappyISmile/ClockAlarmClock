package com.example.clockalarmclock.model;

//import javax.persistence.Entity;
//import javax.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.sql.Timestamp;
import java.util.Set;

@Entity(name = "usersDataTable")
public class User {

    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private Set<Notification> notification;


    public Set<Notification> getNotificatio() {
        return notification;
    }

    public void setNotification(Set<Notification> notification) {
        this.notification = notification;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
