package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;

public interface UserCommandUseCase {

    /**
     * 유저 정보 업데이트 메서드
     *
     * @param user
     * @param request
     * @return
     */
    UserResponseDTO.User updateUserInfo(User user, UserRequestDTO.UserInfoUpdateRequest request);

    /**
     * 회원가입 시 이메일 중복 확인을 위한 메서드
     *
     * @param request
     * @return
     */
    UserResponseDTO.CheckEmailDuplicationResponse checkEmailDuplication(UserRequestDTO.CheckEmailDuplicationRequest request);

    /**
     * 비밀번호 변경 시 기존 비밀번호와 일치하는지 확인을 위한 메서드
     *
     * @param request
     * @param user
     * @return
     */
    UserResponseDTO.CheckOriginalPasswordResponse checkOriginalPassword(UserRequestDTO.CheckOriginalPasswordRequest request, User user);

    /**
     * 유저를 생성하여 DB에 저장하는 메서드
     *
     * @param request
     * @return
     */
    User createUser(UserRequestDTO.SignUpRequest request);

}
