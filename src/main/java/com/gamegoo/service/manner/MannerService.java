package com.gamegoo.service.manner;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MannerHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.manner.MannerKeyword;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.manner.MannerRatingKeyword;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.notification.Notification;
import com.gamegoo.domain.notification.NotificationTypeTitle;
import com.gamegoo.dto.manner.MannerRequest;
import com.gamegoo.dto.manner.MannerResponse;
import com.gamegoo.repository.manner.MannerKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.repository.notification.NotificationRepository;
import com.gamegoo.service.notification.NotificationService;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MannerService {

    private final MemberRepository memberRepository;
    private final MannerRatingRepository mannerRatingRepository;
    private final MannerRatingKeywordRepository mannerRatingKeywordRepository;
    private final MannerKeywordRepository mannerKeywordRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // 매너평가 등록
    public MannerRating insertManner(MannerRequest.mannerInsertDTO request, Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId())
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 본인의 id를 요청한 경우
        if (member.equals(targetMember)) {
            throw new MannerHandler(ErrorStatus.MANNER_INSERT_BAD_REQUEST);
        }

        // 매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(
            member.getId(), targetMember.getId());
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
            .filter(MannerRating::getIsPositive)
            .collect(Collectors.toList());

        if (!positiveMannerRatings.isEmpty()) {
            throw new MannerHandler(ErrorStatus.MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = request.getMannerRatingKeywordList().stream()
            .map(mannerKeywordId -> mannerKeywordRepository.findById(mannerKeywordId)
                .orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND)))
            .peek(mannerKeyword -> {
                if (!mannerKeyword.getIsPositive()) {
                    throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
                }
            })
            .collect(Collectors.toList());

        // manner rating 엔티티 생성 및 연관관계 매핑.
        MannerRating mannerRating = MannerRating.builder()
            .fromMember(member)
            .mannerRatingKeywordList(new ArrayList<>())
            .isPositive(true)
            .build();

        mannerRating.setToMember(targetMember);
        MannerRating saveManner = mannerRatingRepository.save(mannerRating);

        // manner Rating Keyword 엔티티 생성 및 연관관계 매핑.
        mannerRatingKeywordList.forEach(mannerKeyword -> {
            MannerRatingKeyword mannerRatingKeyword = MannerRatingKeyword.builder()
                .mannerKeyword(mannerKeyword)
                .build();

            mannerRatingKeyword.setMannerRating(saveManner);
            mannerRatingKeywordRepository.save(mannerRatingKeyword);
        });

        // 매너평가 등록됨 알림 전송
        // 등록된 매너 평가 키워드 string 생성
        String mannerKeywordString = mannerRatingKeywordList.get(0).getContents();
        if (mannerRatingKeywordList.size() > 1) {
            mannerKeywordString += " 외 " + (mannerRatingKeywordList.size() - 1) + "개의";
        }

        Notification ratedNotification = notificationService.createNotification(
            NotificationTypeTitle.MANNER_KEYWORD_RATED,
            mannerKeywordString, null, targetMember);

        // 매너점수 산정.
        Integer mannerScore = updateMannerScore(targetMember);

        // 매너점수 반영.
        targetMember.setMannerScore(mannerScore);

        // 매너레벨 결정.
        Integer mannerLevel = mannerLevel(mannerScore);

        // 매너레벨 상승 알림 전송
        if (targetMember.getMannerLevel() < mannerLevel) {
            Notification mannerUpNotification = notificationService.createNotification(
                NotificationTypeTitle.MANNER_LEVEL_UP, mannerLevel.toString(),
                null, targetMember);
            notificationRepository.save(mannerUpNotification);
        }

        // 매너레벨 반영.
        targetMember.setMannerLevel(mannerLevel);

        // db 저장.
        memberRepository.save(targetMember);

        return saveManner;
    }

    // 비매너평가 등록
    public MannerRating insertBadManner(MannerRequest.mannerInsertDTO request, Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId())
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 본인의 id를 요청한 경우
        if (member.equals(targetMember)) {
            throw new MannerHandler(ErrorStatus.MANNER_INSERT_BAD_REQUEST);
        }

        // 비매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(
            member.getId(), targetMember.getId());
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
            .filter(rating -> !rating.getIsPositive())
            .collect(Collectors.toList());

        if (!negativeMannerRatings.isEmpty()) {
            throw new MannerHandler(ErrorStatus.BAD_MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = request.getMannerRatingKeywordList().stream()
            .map(mannerKeywordId -> mannerKeywordRepository.findById(mannerKeywordId)
                .orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND)))
            .peek(mannerKeyword -> {
                if (mannerKeyword.getIsPositive()) {
                    throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
                }
            })
            .collect(Collectors.toList());

        // manner rating 엔티티 생성 및 연관관계 매핑.
        MannerRating mannerRating = MannerRating.builder()
            .fromMember(member)
            .mannerRatingKeywordList(new ArrayList<>())
            .isPositive(false)
            .build();

        mannerRating.setToMember(targetMember);
        MannerRating saveManner = mannerRatingRepository.save(mannerRating);

        // manner Rating Keyword 엔티티 생성 및 연관관계 매핑.
        mannerRatingKeywordList.forEach(mannerKeyword -> {
            MannerRatingKeyword mannerRatingKeyword = MannerRatingKeyword.builder()
                .mannerKeyword(mannerKeyword)
                .build();

            mannerRatingKeyword.setMannerRating(saveManner);
            mannerRatingKeywordRepository.save(mannerRatingKeyword);
        });

        // 비매너평가 등록됨 알림 전송
        // 등록된 매너 평가 키워드 string 생성
        String mannerKeywordString = mannerRatingKeywordList.get(0).getContents();
        if (mannerRatingKeywordList.size() > 1) {
            mannerKeywordString += " 외 " + (mannerRatingKeywordList.size() - 1) + "개의";
        }

        Notification ratedNotification = notificationService.createNotification(
            NotificationTypeTitle.MANNER_KEYWORD_RATED,
            mannerKeywordString, null, targetMember);

        notificationRepository.save(ratedNotification);

        // 매너점수 산정.
        Integer mannerScore = updateMannerScore(targetMember);

        // 매너점수 반영.
        targetMember.setMannerScore(mannerScore);

        // 매너레벨 결정.
        Integer mannerLevel = mannerLevel(mannerScore);

        // 매너레벨 하락 알림 전송
        if (targetMember.getMannerLevel() > mannerLevel) {
            Notification mannerDownNotification = notificationService.createNotification(
                NotificationTypeTitle.MANNER_LEVEL_DOWN, mannerLevel.toString(),
                null, targetMember);
            notificationRepository.save(mannerDownNotification);
        }

        // 매너레벨 반영.
        targetMember.setMannerLevel(mannerLevel);

        // db 저장.
        memberRepository.save(targetMember);

        return saveManner;
    }

    // 매너평가 수정.
    @Transactional
    public MannerRating update(MannerRequest.mannerUpdateDTO request, Long memberId,
        Long mannerId) {

        MannerRating mannerRating = mannerRatingRepository.findById(mannerId)
            .orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_NOT_FOUND));

        Member targetMember = memberRepository.findById(mannerRating.getToMember().getId())
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 매너평가 작성자가 맞는지 검증.
        if (!mannerRating.getFromMember().getId().equals(memberId)) {
            throw new MannerHandler(ErrorStatus.MANNER_UNAUTHORIZED);
        }

        // 매너평가
        if (mannerRating.getIsPositive()) {

            // mannerKeyword 의 실제 존재 여부 검증 및 매너평가 키워드인지 검증.
            List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
            request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId)
                        .orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if (!mannerKeyword.getIsPositive()) {
                        throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
                    }
                    mannerRatingKeywordList.add(mannerKeyword);
                });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long, MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList()
                .stream()
                .collect(Collectors.toMap(
                    mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId(),
                    mannerRatingKeyword -> mannerRatingKeyword,
                    (existing, replacement) -> existing));

            Set<Long> newMannerKeywordIds = mannerRatingKeywordList.stream()
                .map(MannerKeyword::getId)
                .collect(Collectors.toSet());

            // 삭제할 엔티티를 검색
            List<MannerRatingKeyword> toRemove = new ArrayList<>();
            for (MannerRatingKeyword existingKeyword : mannerRating.getMannerRatingKeywordList()) {
                if (!newMannerKeywordIds.contains(existingKeyword.getMannerKeyword().getId())) {
                    toRemove.add(existingKeyword);
                }
            }
            toRemove.forEach(mannerRating::removeMannerRatingKeyword);

            // 새로 추가하거나 업데이트할 엔티티
            for (MannerKeyword mannerKeyword : mannerRatingKeywordList) {
                MannerRatingKeyword mannerRatingKeyword = existingMannerRatings.get(
                    mannerKeyword.getId());
                if (mannerRatingKeyword == null) {
                    mannerRatingKeyword = MannerRatingKeyword.builder()
                        .mannerKeyword(mannerKeyword)
                        .build();
                    // 연관 관계 설정
                    mannerRatingKeyword.setMannerRating(mannerRating); // MannerRating 설정
                } else {
                    // 기존 엔티티 업데이트
                    mannerRatingKeyword.setMannerKeyword(mannerKeyword);
                }
            }

            // 매너점수 산정.
            Integer mannerScore = updateMannerScore(targetMember);

            // 매너점수 반영.
            targetMember.setMannerScore(mannerScore);

            // 매너레벨 결정.
            Integer mannerLevel = mannerLevel(mannerScore);

            // 매너레벨 상승 알림 전송
            if (targetMember.getMannerLevel() < mannerLevel) {
                Notification mannerUpNotification = notificationService.createNotification(
                    NotificationTypeTitle.MANNER_LEVEL_UP, mannerLevel.toString(),
                    null, targetMember);
                notificationRepository.save(mannerUpNotification);
            } else if (targetMember.getMannerLevel() > mannerLevel) { // 매너레벨 하락 알림 전송
                Notification mannerDownNotification = notificationService.createNotification(
                    NotificationTypeTitle.MANNER_LEVEL_DOWN, mannerLevel.toString(),
                    null, targetMember);
                notificationRepository.save(mannerDownNotification);
            }

            // 매너레벨 반영.
            targetMember.setMannerLevel(mannerLevel);

            // db 저장.
            memberRepository.save(targetMember);

            return mannerRatingRepository.save(mannerRating);
        }

        // 비매너 평가
        else {

            // mannerKeyword 의 실제 존재 여부 검증 및 비매너평가 키워드인지 검증.
            List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
            request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId)
                        .orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if (mannerKeyword.getIsPositive()) {
                        throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
                    }
                    mannerRatingKeywordList.add(mannerKeyword);
                });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long, MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList()
                .stream()
                .collect(Collectors.toMap(
                    mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId(),
                    mannerRatingKeyword -> mannerRatingKeyword,
                    (existing, replacement) -> existing));

            Set<Long> newMannerKeywordIds = mannerRatingKeywordList.stream()
                .map(MannerKeyword::getId)
                .collect(Collectors.toSet());

            // 삭제할 엔티티를 검색
            List<MannerRatingKeyword> toRemove = new ArrayList<>();
            for (MannerRatingKeyword existingKeyword : mannerRating.getMannerRatingKeywordList()) {
                if (!newMannerKeywordIds.contains(existingKeyword.getMannerKeyword().getId())) {
                    toRemove.add(existingKeyword);
                }
            }
            toRemove.forEach(mannerRating::removeMannerRatingKeyword);

            // 새로 추가하거나 업데이트할 엔티티
            for (MannerKeyword mannerKeyword : mannerRatingKeywordList) {
                MannerRatingKeyword mannerRatingKeyword = existingMannerRatings.get(
                    mannerKeyword.getId());
                if (mannerRatingKeyword == null) {
                    mannerRatingKeyword = MannerRatingKeyword.builder()
                        .mannerKeyword(mannerKeyword)
                        .build();
                    // 연관 관계 설정
                    mannerRatingKeyword.setMannerRating(mannerRating); // MannerRating 설정
                } else {
                    // 기존 엔티티 업데이트
                    mannerRatingKeyword.setMannerKeyword(mannerKeyword);
                }
            }

            // 매너점수 산정.
            Integer mannerScore = updateMannerScore(targetMember);

            // 매너점수 반영.
            targetMember.setMannerScore(mannerScore);

            // 매너레벨 결정.
            Integer mannerLevel = mannerLevel(mannerScore);

            // 매너레벨 상승 알림 전송
            if (targetMember.getMannerLevel() < mannerLevel) {
                Notification mannerUpNotification = notificationService.createNotification(
                    NotificationTypeTitle.MANNER_LEVEL_UP, mannerLevel.toString(),
                    null, targetMember);
                notificationRepository.save(mannerUpNotification);
            } else if (targetMember.getMannerLevel() > mannerLevel) { // 매너레벨 하락 알림 전송
                Notification mannerDownNotification = notificationService.createNotification(
                    NotificationTypeTitle.MANNER_LEVEL_DOWN, mannerLevel.toString(),
                    null, targetMember);
                notificationRepository.save(mannerDownNotification);
            }

            // 매너레벨 반영.
            targetMember.setMannerLevel(mannerLevel);

            // db 저장.
            memberRepository.save(targetMember);

            return mannerRatingRepository.save(mannerRating);
        }
    }

    //매너,비매너 평가 기록 삭제 처리
    @Transactional
    public void deleteMannerRatingsByMemberId(Long memberId) {
        // 탈퇴한 회원이 남긴 매너,비매너 평가 기록
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberId(memberId);

        // 탈퇴한 회원으로부터 매너,비매너 평가를 받은 회원 목록
        List<Member> toMembers = mannerRatings.stream()
                .map(MannerRating::getToMember)
                .distinct()  // 중복된 회원 제거
                .collect(Collectors.toList());

        // 탈퇴한 회원이 남긴 매너.비매너 평가 삭제
        mannerRatingRepository.deleteByMemberId(memberId);

        for (Member member : toMembers){
            // 매너점수 산정.
            Integer mannerScore = updateMannerScore(member);
            // 매너점수 반영.
            member.setMannerScore(mannerScore);
            // 매너레벨 결정.
            Integer mannerLevel = mannerLevel(mannerScore);
            // 매너레벨 상승 알림 전송
            if (member.getMannerLevel() < mannerLevel) {
                Notification mannerUpNotification = notificationService.createNotification(
                        NotificationTypeTitle.MANNER_LEVEL_UP, mannerLevel.toString(),
                        null, member);
                notificationRepository.save(mannerUpNotification);
            } else if (member.getMannerLevel() > mannerLevel) { // 매너레벨 하락 알림 전송
                Notification mannerDownNotification = notificationService.createNotification(
                        NotificationTypeTitle.MANNER_LEVEL_DOWN, mannerLevel.toString(),
                        null, member);
                notificationRepository.save(mannerDownNotification);
            }
            // 매너레벨 반영.
            member.setMannerLevel(mannerLevel);
            // db 저장.
            memberRepository.save(member);
        }
    }

    // 매너점수를 산정하고 업데이트.
    private int updateMannerScore(Member targetMember) {

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = mannerRatingRepository.findByToMemberId(targetMember.getId());

        int totalCount = 0;

        // 내게 평가를 남긴 회원이 모두 탈퇴하여 매너,비매너 평가 이력이 없어진 경우
        if (mannerRatings.size() == 0) {
            totalCount = 0;
        } else if (mannerRatings.size() == 1) {  // 매너 평가 + 비매너 평가를 처음 받은 회원
            if (mannerRatings.get(0).getIsPositive()) {
                totalCount = mannerRatings.get(0).getMannerRatingKeywordList().size();
            } else {
                totalCount = (mannerRatings.get(0).getMannerRatingKeywordList().size()) * -2;
            }
        } else {
            int positiveCount = mannerRatings.stream()
                .filter(MannerRating::getIsPositive)
                .flatMap(mannerRating -> mannerRating.getMannerRatingKeywordList().stream())
                .collect(Collectors.toList())
                .size();

            int negativeCount = mannerRatings.stream()
                .filter(mannerRating -> !mannerRating.getIsPositive())
                .flatMap(mannerRating -> mannerRating.getMannerRatingKeywordList().stream())
                .collect(Collectors.toList())
                .size();

            totalCount = positiveCount + (negativeCount * -2);
        }

        return totalCount;
    }

    // 매너레벨 결정
    private int mannerLevel(int mannerCount) {
        if (mannerCount < 10) {
            return 1;
        } else if (mannerCount < 20) {
            return 2;
        } else if (mannerCount < 30) {
            return 3;
        } else if (mannerCount < 40) {
            return 4;
        } else {
            return 5;
        }
    }

    // 매너평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.mannerKeywordResponseDTO getMannerKeyword(Long memberId,
        Long targetMemberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가를 받은 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        Boolean isExist;

        // 매너평가를 받은 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(
            member.getId(), targetMember.getId());
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
            .filter(MannerRating::getIsPositive)
            .collect(Collectors.toList());

        if (positiveMannerRatings.isEmpty()) {

            isExist = false;

            List<Long> mannerKeywordIds = Collections.emptyList();

            return MannerResponse.mannerKeywordResponseDTO.builder()
                .mannerId(null)
                .isPositive(true)
                .isExist(isExist)
                .mannerRatingKeywordList(mannerKeywordIds)
                .build();

        } else {

            isExist = true;

            MannerRating positiveMannerRating = positiveMannerRatings.get(0);

            List<Long> mannerKeywordIds = positiveMannerRating.getMannerRatingKeywordList().stream()
                .map(mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId())
                .collect(Collectors.toList());

            return MannerResponse.mannerKeywordResponseDTO.builder()
                .mannerId(positiveMannerRating.getId())
                .isPositive(true)
                .isExist(isExist)
                .mannerRatingKeywordList(mannerKeywordIds)
                .build();
        }
    }

    // 비매너평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.badMannerKeywordResponseDTO getBadMannerKeyword(Long memberId,
        Long targetMemberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비매너평가를 받은 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.BAD_MANNER_TARGET_MEMBER_NOT_FOUND));

        Boolean isExist;

        // 비매너평가 ID 조회
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(
            member.getId(), targetMember.getId());
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
            .filter(mannerRating -> !mannerRating.getIsPositive())
            .collect(Collectors.toList());

        if (negativeMannerRatings.isEmpty()) {

            isExist = false;

            List<Long> badMannerKeywordIds = Collections.emptyList();

            return MannerResponse.badMannerKeywordResponseDTO.builder()
                .mannerId(null)
                .isPositive(false)
                .isExist(isExist)
                .mannerRatingKeywordList(badMannerKeywordIds)
                .build();
        } else {

            isExist = true;

            MannerRating negativeMannerRating = negativeMannerRatings.get(0);

            List<Long> badMannerKeywordIds = negativeMannerRating.getMannerRatingKeywordList()
                .stream()
                .map(mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId())
                .collect(Collectors.toList());

            return MannerResponse.badMannerKeywordResponseDTO.builder()
                .mannerId(negativeMannerRating.getId())
                .isPositive(false)
                .isExist(isExist)
                .mannerRatingKeywordList(badMannerKeywordIds)
                .build();
        }
    }

    // 내가 받은 매너 평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.myMannerResponseDTO getMyManner(Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = member.getMannerRatingList();

        // 매너평가 점수
        Integer mannerScore = member.getMannerScore();

        // 매너키워드 조회
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
            .filter(MannerRating::getIsPositive)
            .collect(Collectors.toList());

        // 각 매너키워드(1~6) 별 count 집계
        List<Long> mannerKeywordIds = new ArrayList<>();

        for (MannerRating positiveRating : positiveMannerRatings) {
            List<MannerRatingKeyword> mannerRatingKeywords = positiveRating.getMannerRatingKeywordList();
            for (MannerRatingKeyword mannerRatingKeyword : mannerRatingKeywords) {
                mannerKeywordIds.add(mannerRatingKeyword.getMannerKeyword().getId());
            }
        }

        Map<Integer, Integer> mannerKeywordCountMap = new HashMap<>();
        for (long i = 1; i <= 6; i++) {
            mannerKeywordCountMap.put((int) i, 0); // 초기화
        }
        for (Long keywordId : mannerKeywordIds) {
            mannerKeywordCountMap.put(keywordId.intValue(),
                mannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
        }

        // 비매너키워드 조회
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
            .filter(mannerRating -> !mannerRating.getIsPositive())
            .collect(Collectors.toList());

        // 각 비매너키워드(7~12) 별 count 집계
        List<Long> badMannerKeywordIds = new ArrayList<>();

        for (MannerRating negativeRating : negativeMannerRatings) {
            List<MannerRatingKeyword> badMannerRatingKeywords = negativeRating.getMannerRatingKeywordList();
            for (MannerRatingKeyword badMannerRatingKeyword : badMannerRatingKeywords) {
                badMannerKeywordIds.add(badMannerRatingKeyword.getMannerKeyword().getId());
            }
        }

        Map<Integer, Integer> badMannerKeywordCountMap = new HashMap<>();
        for (long i = 7; i <= 12; i++) {
            badMannerKeywordCountMap.put((int) i, 0); // 초기화
        }
        for (Long keywordId : badMannerKeywordIds) {
            badMannerKeywordCountMap.put(keywordId.intValue(),
                badMannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
        }

        // 매너 키워드 DTO 생성
        List<MannerResponse.mannerKeywordDTO> mannerKeywordDTOs = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            int count = mannerKeywordCountMap.getOrDefault(i, 0);
            mannerKeywordDTOs.add(new MannerResponse.mannerKeywordDTO(true, i, count));
        }

        // 비매너 키워드 DTO 생성
        for (int i = 7; i <= 12; i++) {
            int count = badMannerKeywordCountMap.getOrDefault(i, 0);
            mannerKeywordDTOs.add(new MannerResponse.mannerKeywordDTO(false, i, count));
        }

        List<MannerResponse.mannerKeywordDTO> mannerKeywords = sortMannerKeywordDTOs(
            mannerKeywordDTOs);

        Integer mannerLevel = member.getMannerLevel();

        Double mannerRank=getMannerScoreRank(member.getId());

        return MannerResponse.myMannerResponseDTO.builder()
            .mannerLevel(mannerLevel)
            .mannerKeywords(mannerKeywords)
            .mannerScore(mannerScore)
            .mannerRank(mannerRank)
            .build();
    }

    // 대상 회원의 매너 평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.mannerByIdResponseDTO getMannerById(Long targetMemberId) {

        Member targetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<MannerResponse.mannerKeywordDTO> mannerKeywordDTOs = mannerKeyword(targetMember);

        List<MannerResponse.mannerKeywordDTO> mannerKeywords = sortMannerKeywordDTOs(
            mannerKeywordDTOs);

        Integer mannerLevel = targetMember.getMannerLevel();
        return MannerResponse.mannerByIdResponseDTO.builder()
            .memberId(targetMember.getId())
            .mannerLevel(mannerLevel)
            .mannerKeywords(mannerKeywords)
            .build();
    }

    public List<MannerResponse.mannerKeywordDTO> mannerKeyword(Member targetMember) {
        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = targetMember.getMannerRatingList();

        // 매너키워드 조회
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
            .filter(MannerRating::getIsPositive)
            .collect(Collectors.toList());

        // 각 매너키워드(1~6) 별 count 집계
        List<Long> mannerKeywordIds = new ArrayList<>();

        for (MannerRating positiveRating : positiveMannerRatings) {
            List<MannerRatingKeyword> mannerRatingKeywords = positiveRating.getMannerRatingKeywordList();
            for (MannerRatingKeyword mannerRatingKeyword : mannerRatingKeywords) {
                mannerKeywordIds.add(mannerRatingKeyword.getMannerKeyword().getId());
            }
        }

        Map<Integer, Integer> mannerKeywordCountMap = new HashMap<>();
        for (long i = 1; i <= 6; i++) {
            mannerKeywordCountMap.put((int) i, 0); // 초기화
        }
        for (Long keywordId : mannerKeywordIds) {
            mannerKeywordCountMap.put(keywordId.intValue(),
                mannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
        }

        // 비매너키워드 조회
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
            .filter(mannerRating -> !mannerRating.getIsPositive())
            .collect(Collectors.toList());

        // 각 비매너키워드(7~12) 별 count 집계
        List<Long> badMannerKeywordIds = new ArrayList<>();

        for (MannerRating negativeRating : negativeMannerRatings) {
            List<MannerRatingKeyword> badMannerRatingKeywords = negativeRating.getMannerRatingKeywordList();
            for (MannerRatingKeyword badMannerRatingKeyword : badMannerRatingKeywords) {
                badMannerKeywordIds.add(badMannerRatingKeyword.getMannerKeyword().getId());
            }
        }

        Map<Integer, Integer> badMannerKeywordCountMap = new HashMap<>();
        for (long i = 7; i <= 12; i++) {
            badMannerKeywordCountMap.put((int) i, 0); // 초기화
        }
        for (Long keywordId : badMannerKeywordIds) {
            badMannerKeywordCountMap.put(keywordId.intValue(),
                badMannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
        }

        // 매너 키워드 DTO 생성
        List<MannerResponse.mannerKeywordDTO> mannerKeywordDTOs = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            int count = mannerKeywordCountMap.getOrDefault(i, 0);
            mannerKeywordDTOs.add(new MannerResponse.mannerKeywordDTO(true, i, count));
        }

        // 비매너 키워드 DTO 생성
        for (int i = 7; i <= 12; i++) {
            int count = badMannerKeywordCountMap.getOrDefault(i, 0);
            mannerKeywordDTOs.add(new MannerResponse.mannerKeywordDTO(false, i, count));
        }

        return mannerKeywordDTOs;
    }

    // mannerKeywordDTOs(매너키워드,비매너키워드) 정렬
    public List<MannerResponse.mannerKeywordDTO> sortMannerKeywordDTOs(
        List<MannerResponse.mannerKeywordDTO> mannerKeywordDTOs) {

        // mannerKeywordId와 contents 값을 매핑
        Map<Long, String> content = mannerKeywordRepository.findAll().stream()
            .collect(Collectors.toMap(MannerKeyword::getId, MannerKeyword::getContents));

        // Comparator 생성
        // 우선 순위) 1. count 기준 내림차순, 2.contents 기준 숫자, 3.contents 기준 한글(ㄱㄴㄷ순) 정렬
        Comparator<MannerResponse.mannerKeywordDTO> comparator = Comparator
            .comparingInt(MannerResponse.mannerKeywordDTO::getCount).reversed() // count 내림차순
            .thenComparing(dto -> {
                String contents = content.get((long) dto.getMannerKeywordId());
                return sortByContents(contents);
            }); // contents 기준 정렬

        // 매너키워드와 비매너키워드를 분리하여 각각 정렬
        List<MannerResponse.mannerKeywordDTO> positiveKeywords = mannerKeywordDTOs.stream()
            .filter(MannerResponse.mannerKeywordDTO::getIsPositive)
            .sorted(comparator)
            .collect(Collectors.toList());

        List<MannerResponse.mannerKeywordDTO> negativeKeywords = mannerKeywordDTOs.stream()
            .filter(dto -> !dto.getIsPositive())
            .sorted(comparator)
            .collect(Collectors.toList());

        // 정렬된 매너키워드와 비매너키워드를 합치기
        List<MannerResponse.mannerKeywordDTO> sortedKeywordDTOs = new ArrayList<>();
        sortedKeywordDTOs.addAll(positiveKeywords);
        sortedKeywordDTOs.addAll(negativeKeywords);

        return sortedKeywordDTOs;
    }

    // contents를 숫자 우선, 한글로 정렬하는 메서드
    public String sortByContents(String contents) {
        if (contents == null || contents.isEmpty()) {
            // contents가 null이거나 빈 문자열인 경우 가장 낮은 우선순위로 처리
            return "\uFFFF"; // ASCII의 가장 큰 값
        }

        // 첫 글자 가져오기
        char firstChar = contents.charAt(0);

        if (Character.isDigit(firstChar)) {
            // 숫자일 경우, 우선순위 높음 (숫자를 기준으로 정렬)
            return "0" + contents; // 숫자로 시작하는 경우를 우선순위가 높은 것으로 설정
        } else {
            // 한글일 경우, 한글 정렬을 위해 `contents` 자체를 반환
            return contents;
        }
    }

    // 회원의 매너점수가 전체 회원 중 상위 몇 퍼센트에 위치하는지 계산
    public Double getMannerScoreRank(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMannerScore()==null){
            return null;
        } else {
            // repository에서 탈퇴하지 않은 회원 중에서 매너점수가 null이 아닌 회원만 가져오기
            long countMembersWithMannerScore = memberRepository.countByMannerScoreIsNotNullAndBlindFalse();

            // repository에서 탈퇴하지 않은 회원 중에서 매너점수가 내 매너점수보다 큰 회원 수 조회
            long countHigherMannerScores = memberRepository.countByMannerScoreGreaterThanAndBlindFalse(member.getMannerScore());

            // 계산 불가
            if (countMembersWithMannerScore == 0){
                throw new MannerHandler(ErrorStatus.MANNER_RANK_FAILED_MANNER_SCORE_NOT_FOUND);
            }

            // 특정 회원의 매너점수가 상위 몇 %인지 계산
            double mannerRank = ((double) countHigherMannerScores / countMembersWithMannerScore) * 100;

            return mannerRank;
        }
    }

    // 회원에게 매너평가를 한 사람의 수
    public Long getMannerRatingCount(Long memberId){
        Long mannerRatingCount = mannerRatingRepository.countDistinctFromMemberByToMemberId(memberId);
        return mannerRatingCount;
    }

}
