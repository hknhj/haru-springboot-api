package com.haru.api.workspace.application.service;

import com.haru.api.common_library.application.port.in.DocumentQueryUseCase;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.user.application.port.in.UserQueryUseCase;
import com.haru.api.user.application.converter.UserDocumentLastOpenedConverter;
import com.haru.api.workspace.application.port.in.WorkspaceCommandUseCase;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.application.port.out.WorkspaceInvitationPort;
import com.haru.api.workspace.application.port.out.WorkspacePort;
import com.haru.api.common_library.domain.Documentable;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.application.converter.WorkspaceConverter;
import com.haru.api.workspace.presentation.dto.WorkspaceRequestDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.domain.WorkspaceInvitation;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.UserWorkspaceHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceInvitationHandler;
import com.haru.api.infra.mail.EmailSender;
import com.haru.api.infra.s3.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceCommandUseCaseImpl implements WorkspaceCommandUseCase {

    private final UserQueryUseCase userQueryUseCase;
    private final UserDocumentLastOpenedCommandUseCase userDocumentLastOpenedCommandUseCase;
    private final DocumentQueryUseCase documentQueryUseCase;

    private final WorkspacePort workspacePort;
    private final UserWorkspacePort userWorkspacePort;
    private final WorkspaceInvitationPort workspaceInvitationPort;

    private final AmazonS3Manager amazonS3Manager;
    private final EmailSender emailSender;

    @Value("${invite-url}")
    private String inviteBaseUrl;

    @Transactional
    @Override
    public WorkspaceResponseDTO.Workspace createWorkspace(User user, WorkspaceRequestDTO.WorkspaceCreateRequest request, MultipartFile image) {

        String keyName = null;

        if (image != null) {
            // s3에 사진 추가하는 메서드
            String path = amazonS3Manager.generateKeyName("workspace/image");
            keyName = amazonS3Manager.uploadMultipartFile(path, image);
        }

        // workspace entity 생성
        Workspace workspace = workspacePort.save(Workspace.builder()
                .title(request.getTitle())
                .keyName(keyName)
                .build());

        // users_workspaces 테이블에 생성자 정보 저장
        userWorkspacePort.save(UserWorkspace.builder()
                .user(user)
                .workspace(workspace)
                .auth(Auth.ADMIN)
                .build());

        return WorkspaceConverter.toWorkspaceDTO(workspace, amazonS3Manager.generatePresignedUrl(keyName));
    }

    @Transactional
    @Override
    public WorkspaceResponseDTO.Workspace updateWorkspace(User user, Workspace workspace, WorkspaceRequestDTO.WorkspaceUpdateRequest request, MultipartFile image) {

        UserWorkspace userWorkspace = userWorkspacePort.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        if(userWorkspace.getAuth() != Auth.ADMIN)
            throw new WorkspaceHandler(ErrorStatus.WORKSPACE_MODIFY_NOT_ALLOWED);

        String keyName = workspace.getKeyName();

        if(keyName == null){
            keyName = amazonS3Manager.generateKeyName("workspace/image");
            workspace.initKeyName(keyName);
        }

        // 제목 수정
        workspace.updateTitle(request.getTitle());

        // 이미지 수정
        if (image != null) {
            amazonS3Manager.uploadMultipartFile(keyName, image);
        }

        workspacePort.save(workspace);

        return WorkspaceConverter.toWorkspaceDTO(workspace, amazonS3Manager.generatePresignedUrl(workspace.getKeyName()));
    }

    @Override
    @Transactional
    public WorkspaceResponseDTO.InvitationAcceptResult acceptInvite(String token) {

        WorkspaceInvitation foundWorkspaceInvitation = workspaceInvitationPort.findByToken(token)
                .orElseThrow(() -> new WorkspaceInvitationHandler(ErrorStatus.INVITATION_NOT_FOUND));

        Workspace foundWorkspace = foundWorkspaceInvitation.getWorkspace();

        // 이미 수락된 초대장이면 예외 발생
        if(foundWorkspaceInvitation.isAccepted())
            throw new WorkspaceInvitationHandler(ErrorStatus.ALREADY_ACCEPTED);

        // 초대받은 이메일로 가입된 사용자가 있는지 확인
        Optional<User> foundUser = userQueryUseCase.findOptionalUserByEmail(foundWorkspaceInvitation.getEmail());

        boolean isAlreadyRegistered = foundUser.isPresent();

        // 이미 가입된 사용자
        if(isAlreadyRegistered) {
            // 초대장을 수락했다고 db에 저장
            foundWorkspaceInvitation.setAccepted();
            workspaceInvitationPort.save(foundWorkspaceInvitation);
        } else {
            // 가입되지 않은 사용자면 not success
            return WorkspaceConverter.toInvitationAcceptResult(false, false, foundWorkspace);
        }

        addUserToWorkspaceAndSetupDocuments(foundWorkspace, foundUser.get());

        return WorkspaceConverter.toInvitationAcceptResult(true, true, foundWorkspace);
    }

    @Transactional
    @Override
    public void acceptInvite(String token, User signedUser) {
        WorkspaceInvitation foundWorkspaceInvitation = workspaceInvitationPort.findByToken(token)
                .orElseThrow(() -> new WorkspaceInvitationHandler(ErrorStatus.INVITATION_NOT_FOUND));

        Workspace foundWorkspace = foundWorkspaceInvitation.getWorkspace();

        if(foundWorkspaceInvitation.isAccepted())
            throw new WorkspaceInvitationHandler(ErrorStatus.ALREADY_ACCEPTED);

        foundWorkspaceInvitation.setAccepted();
        workspaceInvitationPort.save(foundWorkspaceInvitation);

        addUserToWorkspaceAndSetupDocuments(foundWorkspace, signedUser);
    }

    @Transactional
    @Override
    public void sendInviteEmail(User user, WorkspaceRequestDTO.WorkspaceInviteEmailRequest request) {

        Workspace foundWorkspace = workspacePort.findById(request.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceHandler(ErrorStatus.WORKSPACE_NOT_FOUND));

        userWorkspacePort.findByUserIdAndWorkspaceId(user.getId(), foundWorkspace.getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        // 이메일마다 invitation 생성하여 저장, 초대 수락 토큰 생성, 초대 이메일 발송
        for(String email: request.getEmails()) {
            String token = UUID.randomUUID().toString();

            WorkspaceInvitation workspaceInvitation = WorkspaceInvitation.builder()
                    .email(email)
                    .workspace(foundWorkspace)
                    .token(token)
                    .isAccepted(false)
                    .build();
            workspaceInvitationPort.save(workspaceInvitation);

            String invitationLink = inviteBaseUrl + "?token=" + token;

            String subject = String.format("[%s] 에서 [%s] 님이 당신을 초대했어요!", foundWorkspace.getTitle(), user.getName());
            String content = generateInvitationEmailContentHtml(email, user.getName(), foundWorkspace.getTitle(), invitationLink);

            emailSender.send(email, subject, content);
        }

    }

    private void addUserToWorkspaceAndSetupDocuments(Workspace workspace, User user) {

        // 워크스페이스에 사용자 추가
        userWorkspacePort.save(UserWorkspace.builder()
                .workspace(workspace)
                .user(user)
                .auth(Auth.MEMBER)
                .build());

        // 워크스페이스에 속한 문서를 유저의 UserDocumentLastOpened에 추가
        List<Documentable> documentList = documentQueryUseCase.getDocumentsByWorkspaceId(workspace.getId());

        List<UserDocumentLastOpened> userDocumentLastOpenedList = new ArrayList<>();
        for(Documentable documentable : documentList)
            userDocumentLastOpenedList.add(UserDocumentLastOpenedConverter.toUserDocumentLastOpened(documentable, user));

        userDocumentLastOpenedCommandUseCase.saveAll(userDocumentLastOpenedList);
    }

    private String generateInvitationEmailContentHtml(String invitedEmail, String inviterName, String workspaceName, String invitationLink) {
        return String.format(
                "<html>" +
                        "<head></head>" +
                        "<body>" +
                        "  <p>안녕하세요, %s님,</p>" +
                        "  <p>%s님께서 <b>%s</b> 워크스페이스에 당신을 초대했습니다.</p>" +
                        "  <p>아래 버튼을 클릭하여 워크스페이스에 합류해 주세요!</p>" +
                        "  <p style=\"margin-top: 20px;\">" + // 버튼 스타일
                        "    <a href=\"%s\" " +
                        "       style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: #E65787; text-decoration: none; border-radius: 5px;\">" +
                        "      초대 수락하기" +
                        "    </a>" +
                        "  </p>" +
                        "  <p style=\"margin-top: 30px;\">감사합니다.<br/><b>Team HaRu 드림</b></p>" +
                        "</body>" +
                        "</html>",
                invitedEmail, inviterName, workspaceName, invitationLink
        );
    }

}
