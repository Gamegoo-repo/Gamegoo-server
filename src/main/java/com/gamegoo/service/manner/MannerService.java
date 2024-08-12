package com.gamegoo.service.manner;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MannerHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.manner.MannerKeyword;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.manner.MannerRatingKeyword;
import com.gamegoo.dto.manner.MannerRequest;
import com.gamegoo.dto.manner.MannerResponse;
import com.gamegoo.repository.manner.MannerKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MannerService {
    private final MemberRepository memberRepository;
    private final MannerRatingRepository mannerRatingRepository;
    private final MannerRatingKeywordRepository mannerRatingKeywordRepository;
    private final MannerKeywordRepository mannerKeywordRepository;

    // 매너평가 등록
    public MannerRating insertManner(MannerRequest.mannerInsertDTO request, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId()).orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
                .filter(MannerRating::getIsPositive)
                .collect(Collectors.toList());

        if (!positiveMannerRatings.isEmpty()) {
            throw new MannerHandler(ErrorStatus.MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if (!mannerKeyword.getIsPositive()) {
                        throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
                    }
                    mannerRatingKeywordList.add(mannerKeyword);
                });

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

        // 매너점수 산정.
        int mannerScore = updateMannerScore(targetMember);

        // 매너레벨 결정.
        int mannerLevel = mannerLevel(mannerScore);

        // 매너레벨 반영.
        targetMember.setMannerLevel(mannerLevel);

        // db 저장.
        memberRepository.save(targetMember);

        return saveManner;
    }

    // 비매너평가 등록
    public MannerRating insertBadManner(MannerRequest.mannerInsertDTO request, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId()).orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 비매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
                .filter(rating -> !rating.getIsPositive())
                .collect(Collectors.toList());

        if (!negativeMannerRatings.isEmpty()) {
            throw new MannerHandler(ErrorStatus.BAD_MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if (mannerKeyword.getIsPositive()) {
                        throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
                    }
                    mannerRatingKeywordList.add(mannerKeyword);
                });

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

        // 매너점수 산정.
        int mannerScore = updateMannerScore(targetMember);

        // 매너레벨 결정.
        int mannerLevel = mannerLevel(mannerScore);

        // 매너레벨 반영.
        targetMember.setMannerLevel(mannerLevel);

        // db 저장.
        memberRepository.save(targetMember);

        return saveManner;
    }

    // 매너평가 수정.
    @Transactional
    public MannerRating update(MannerRequest.mannerUpdateDTO request, Long memberId, Long mannerId) {

        MannerRating mannerRating = mannerRatingRepository.findById(mannerId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_NOT_FOUND));

        Member targetMember = memberRepository.findById(mannerRating.getToMember().getId()).orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

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
                        MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                        if (!mannerKeyword.getIsPositive()) {
                            throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
                        }
                        mannerRatingKeywordList.add(mannerKeyword);
                    });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long, MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList().stream()
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
                MannerRatingKeyword mannerRatingKeyword = existingMannerRatings.get(mannerKeyword.getId());
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
            int mannerScore = updateMannerScore(targetMember);

            // 매너레벨 결정.
            int mannerLevel = mannerLevel(mannerScore);

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
                        MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                        if (mannerKeyword.getIsPositive()) {
                            throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
                        }
                        mannerRatingKeywordList.add(mannerKeyword);
                    });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long, MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList().stream()
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
                MannerRatingKeyword mannerRatingKeyword = existingMannerRatings.get(mannerKeyword.getId());
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
            int mannerScore = updateMannerScore(targetMember);

            // 매너레벨 결정.
            int mannerLevel = mannerLevel(mannerScore);

            // 매너레벨 반영.
            targetMember.setMannerLevel(mannerLevel);

            // db 저장.
            memberRepository.save(targetMember);

            return mannerRatingRepository.save(mannerRating);
        }
    }

    // 매너점수를 산정하고 업데이트.
    private int updateMannerScore(Member targetMember){

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = targetMember.getMannerRatingList();

        int totalCount;

        // 매너 평가 + 비매너 평가를 처음 받은 회원
        if (mannerRatings.size()==1){
            if (mannerRatings.get(0).getIsPositive()) {
                totalCount = mannerRatings.get(0).getMannerRatingKeywordList().size();
            } else {
                totalCount = (mannerRatings.get(0).getMannerRatingKeywordList().size())*-2;
            }
        } else {
            int positiveCount = mannerRatings.stream()
                    .filter(MannerRating::getIsPositive)
                    .flatMap(mannerRating -> mannerRating.getMannerRatingKeywordList().stream())
                    .collect(Collectors.toList())
                    .size();

            int negativeCount =  mannerRatings.stream()
                    .filter(mannerRating -> !mannerRating.getIsPositive())
                    .flatMap(mannerRating -> mannerRating.getMannerRatingKeywordList().stream())
                    .collect(Collectors.toList())
                    .size();

            totalCount = positiveCount + (negativeCount*-2);
        }

        return totalCount;
    }

    // 매너레벨 결정
    private int mannerLevel(int mannerCount){
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
    public MannerResponse.mannerKeywordResponseDTO getMannerKeyword(Long memberId, Long targetMemberId){

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가를 받은 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(targetMemberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        Boolean isExist;

        // 매너평가를 받은 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
                .filter(MannerRating::getIsPositive)
                .collect(Collectors.toList());

        if (positiveMannerRatings.isEmpty()) {

            isExist = false;

            List<Long> mannerKeywordIds = Collections.emptyList();

            return MannerResponse.mannerKeywordResponseDTO.builder()
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
                    .isPositive(true)
                    .isExist(isExist)
                    .mannerRatingKeywordList(mannerKeywordIds)
                    .build();
        }
    }

    // 비매너평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.badMannerKeywordResponseDTO getBadMannerKeyword(Long memberId, Long targetMemberId){

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비매너평가를 받은 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(targetMemberId).orElseThrow(() -> new MemberHandler(ErrorStatus.BAD_MANNER_TARGET_MEMBER_NOT_FOUND));

        Boolean isExist;

        // 비매너평가 ID 조회
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
                .filter(mannerRating -> !mannerRating.getIsPositive())
                .collect(Collectors.toList());

        if (negativeMannerRatings.isEmpty()) {

            isExist = false;

            List<Long> badMannerKeywordIds = Collections.emptyList();

            return MannerResponse.badMannerKeywordResponseDTO.builder()
                    .isPositive(false)
                    .isExist(isExist)
                    .mannerRatingKeywordList(badMannerKeywordIds)
                    .build();
        } else {

            isExist = true;

            MannerRating negativeMannerRating = negativeMannerRatings.get(0);

            List<Long> badMannerKeywordIds = negativeMannerRating.getMannerRatingKeywordList().stream()
                    .map(mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId())
                    .collect(Collectors.toList());

            return MannerResponse.badMannerKeywordResponseDTO.builder()
                    .isPositive(false)
                    .isExist(isExist)
                    .mannerRatingKeywordList(badMannerKeywordIds)
                    .build();
        }
    }

    // 내가 받은 매너 평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.myMannerResponseDTO getMyManner(Long memberId){

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가 ID 조회
        List<MannerRating> mannerRatings = member.getMannerRatingList();

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
            mannerKeywordCountMap.put(keywordId.intValue(), mannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
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
            badMannerKeywordCountMap.put(keywordId.intValue(), badMannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
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

        Integer mannerLevel = member.getMannerLevel();
        return MannerResponse.myMannerResponseDTO.builder()
                .mannerLevel(mannerLevel)
                .mannerKeywords(mannerKeywordDTOs)
                .build();
    }

    // 대상 회원의 매너 평가 조회
    @Transactional(readOnly = true)
    public MannerResponse.mannerByIdResponseDTO getMannerById(Long targetMemberId){

        Member targetMember = memberRepository.findById(targetMemberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
            mannerKeywordCountMap.put(keywordId.intValue(), mannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
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
            badMannerKeywordCountMap.put(keywordId.intValue(), badMannerKeywordCountMap.getOrDefault(keywordId.intValue(), 0) + 1);
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

        Integer mannerLevel = targetMember.getMannerLevel();
        return MannerResponse.mannerByIdResponseDTO.builder()
                .memberId(targetMember.getId())
                .mannerLevel(mannerLevel)
                .mannerKeywords(mannerKeywordDTOs)
                .build();
    }
}
