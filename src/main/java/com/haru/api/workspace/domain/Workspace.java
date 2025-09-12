package com.haru.api.workspace.domain;

import com.haru.api.meeting.domain.Meeting;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspaces")
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Workspace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String keyName;

    private String instagramId;

    @Column(columnDefinition="TEXT")
    private String instagramAccessToken;

    @Column(length = 50)
    private String instagramAccountName;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void initKeyName(String keyName) {this.keyName = keyName;}

    public void saveInstagramId(String userId) {
        this.instagramId = userId;
    }

    public void saveInstagramAccessToken(String longLivedAccessToken) {
        this.instagramAccessToken = longLivedAccessToken;
    }

    public void saveInstagramAccountName(String username) {
        this.instagramAccountName = username;
    }
}
