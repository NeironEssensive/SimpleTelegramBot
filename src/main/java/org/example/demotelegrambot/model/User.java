package org.example.demotelegrambot.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
@Entity(name = "tgusers")
@Table(name = "tgusers")
@Getter
@Setter
@ToString
public class User {
    @Id
    @Column(name = "chat_id")
    private long chatId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "username")
    private String username;
    @Column(name = "registered_at")
    private Timestamp registeredAt;
}
