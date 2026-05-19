package com.example.skhubox.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    DUPLICATE_STUDENT_NUMBER(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 학번입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U003", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U004", "비밀번호가 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "U005", "로그인 인증에 실패했습니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "U006", "관리자만 접근할 수 있습니다."),
    CANNOT_WITHDRAW_ADMIN(HttpStatus.BAD_REQUEST, "U012", "관리자 계정은 강제 탈퇴할 수 없습니다."),
    CANNOT_WITHDRAW_SELF(HttpStatus.BAD_REQUEST, "U013", "자기 자신을 탈퇴 처리할 수 없습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "U007", "이메일 인증이 완료되지 않았습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "U008", "유효하지 않은 인증 코드입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U009", "이메일 발송에 실패했습니다."),
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "U010", "유효하지 않거나 만료된 비밀번호 재설정 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "U011", "유효하지 않거나 만료된 리프레시 토큰입니다."),

    LOCKER_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "사물함 정보가 존재하지 않습니다."),
    LOCKER_NOT_NORMAL(HttpStatus.BAD_REQUEST, "L002", "해당 사물함은 현재 사용 불가 상태입니다."),
    ALREADY_RESERVED_LOCKER(HttpStatus.CONFLICT, "L003", "이미 다른 사용자가 이용 중인 사물함입니다."),
    USER_ALREADY_HAS_LOCKER(HttpStatus.BAD_REQUEST, "L004", "이미 이용 중인 사물함이 있습니다."),
    NO_ACTIVE_RESERVATION(HttpStatus.NOT_FOUND, "L005", "현재 이용 중인 사물함 예약 내역이 없습니다."),
    SAME_LOCKER_CHANGE(HttpStatus.BAD_REQUEST, "L006", "동일한 사물함으로 변경할 수 없습니다."),
    QUEUE_MODE_RESERVATION_BLOCKED(HttpStatus.BAD_REQUEST, "L007", "현재 대기열 모드가 활성화되어 있어 바로 예약할 수 없습니다. 대기열을 통해 진행해주세요."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "L008", "해당 예약 정보를 찾을 수 없습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "L009", "다른 사람이 이미 예약을 시도 중입니다."),

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "존재하지 않는 공지사항입니다."),

    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "존재하지 않는 민원입니다."),
    COMPLAINT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "해당 민원에 접근 권한이 없습니다."),
    COMPLAINT_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "C005", "대기중 상태인 민원만 취소할 수 있습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "해당 알림에 접근 권한이 없습니다."),
    COMPLAINT_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "C007", "이미 완료 또는 종료된 민원입니다."),
    COMPLAINT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "C008", "관리자가 설정할 수 없는 민원 상태입니다."),

    QUEUE_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "대기열 모드 설정 정보를 찾을 수 없습니다."),
    QUEUE_MODE_NOT_ENABLED(HttpStatus.BAD_REQUEST, "Q002", "대기열 모드가 활성화되어 있지 않습니다."),
    QUEUE_FULL(HttpStatus.TOO_MANY_REQUESTS, "Q003", "대기열이 가득 찼습니다. 잠시 후 다시 시도해주세요. (최대 500명)");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
