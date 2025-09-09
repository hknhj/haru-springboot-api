package com.haru.api.moodTracker.domain;

import com.haru.api.global.common.Documentable;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.moodTracker.domain.enums.MoodTrackerVisibility;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mood_trackers")
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MoodTracker extends BaseEntity implements Documentable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 설문ID

    // 🔹 만든 사람 (User)와 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    // 🔹 작업공간과 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(length = 50)
    private String title; // 설문명

    @Column(columnDefinition = "TEXT")
    private String description; // 설문소개

    @Column(name = "due_date")
    private LocalDateTime dueDate; // 마감일

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MoodTrackerVisibility visibility; // 공개범위 (PUBLIC, PRIVATE)

    @Column(columnDefinition = "TEXT")
    private String report; // 리포트

    @Min(0)
    private Integer respondentsNum; // 답변자 수

    @Column(columnDefinition = "TEXT")
    private String pdfReportKey;

    @Column(columnDefinition = "TEXT")
    private String wordReportKey;

    @Column(columnDefinition = "TEXT")
    private String thumbnailKeyName;

    @OneToMany(mappedBy = "moodTracker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyQuestion> surveyQuestionList = new ArrayList<>();

    public void updateTitle(String title) {
        this.title = title;
    }

    public void createReport(String report) { this.report = report; }

    @Override
    public Long getWorkspaceId() {
        return this.getWorkspace().getId();
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.TEAM_MOOD_TRACKER;
    }

    @Override
    public String getThumbnailKeyName() {
        return this.thumbnailKeyName;
    }

    public void updateReportKeyName(
            String pdfReportKey,
            String wordReportKey
            ) {
        this.pdfReportKey = pdfReportKey;
        this.wordReportKey = wordReportKey;
    }

    public void updateThumbnailKey(String thumbnailKey) {
        this.thumbnailKeyName = thumbnailKey;
    }
}
