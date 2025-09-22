package com.haru.api.snsEvent.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "participants")
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sns_event_id")
    private SnsEvent snsEvent;

    public void setSnsEvent(SnsEvent snsEvent) {
        this.snsEvent = snsEvent;
        if (this.snsEvent != null) {
            snsEvent.getParticipantList().remove(this);
        }
        this.snsEvent.getParticipantList().add(this);
    }
}
