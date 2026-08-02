package com.userManagement.demo.model;

import jakarta.persistence.*;
import java.time.Instant;


@Entity

@Table(
    name= "tokens"
)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="token_hash", length =64)
    private String tokenHash;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name= "token_type" , nullable = false)
    private TokenType tokenType;

    public  enum TokenType{
        EMAIL_VERIFICATION
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey =@ForeignKey(name ="fk_token_user"))
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name ="used_at")
    private Instant usedAt;


    public Token(){

    }

    public Token(String tokenHash,String email, TokenType tokenType, User user, Instant createdAt, Instant expiresAt) {
        
        this.tokenHash = tokenHash;
        this.email = email;
        this.tokenType= tokenType;
        this.user = user;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }


    @PrePersist
    void onCreate() {
        if(this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Instant getUsedAt(){
        return usedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public User getUser(){
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }
}