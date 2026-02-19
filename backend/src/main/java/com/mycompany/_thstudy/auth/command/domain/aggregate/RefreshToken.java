package com.mycompany._thstudy.auth.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @Column(nullable = false, unique = true, length = 100)
  private String userEmail;

  @Column(nullable = false, length = 512)
  private String token;

  @Column(nullable = false)
  private Date expiryDate;

}
