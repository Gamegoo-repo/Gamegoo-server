package com.gamegoo.service.manner;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BoardHandler;
import com.gamegoo.apiPayload.exception.handler.MannerHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.manner.MannerKeyword;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.manner.MannerRatingKeyword;
import com.gamegoo.dto.manner.MannerRequest;
import com.gamegoo.repository.manner.MannerKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingKeywordRepository;
import com.gamegoo.repository.manner.MannerRatingRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MannerService {
    private final MemberRepository memberRepository;
    private final MannerRatingRepository mannerRatingRepository;
    private final MannerRatingKeywordRepository mannerRatingKeywordRepository;
    private final MannerKeywordRepository mannerKeywordRepository;

    public MannerRating insertManner(MannerRequest.mannerInsertDTO request, Long memberId){

        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId()).orElseThrow(()->new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()){
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> positiveMannerRatings = mannerRatings.stream()
                .filter(MannerRating::getIsPositive)
                .collect(Collectors.toList());

        if(!positiveMannerRatings.isEmpty()){
            throw new MannerHandler(ErrorStatus.MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if(!mannerKeyword.getIsPositive()){
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
        return saveManner;
    }

    public MannerRating insertBadManner(MannerRequest.mannerInsertDTO request, Long memberId){

        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getToMemberId()).orElseThrow(()->new MemberHandler(ErrorStatus.MANNER_TARGET_MEMBER_NOT_FOUND));

        // 비매너평가를 받는 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()){
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 비매너평가 최초 시도 여부 검증.
        List<MannerRating> mannerRatings = mannerRatingRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        List<MannerRating> negativeMannerRatings = mannerRatings.stream()
                .filter(rating -> !rating.getIsPositive())
                .collect(Collectors.toList());

        if(!negativeMannerRatings.isEmpty()){
            throw new MannerHandler(ErrorStatus.BAD_MANNER_CONFLICT);
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    if(mannerKeyword.getIsPositive()){
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
        return saveManner;
    }

    // 매너평가 수정.
    @Transactional
    public MannerRating update(MannerRequest.mannerUpdateDTO request, Long memberId, Long mannerId) {

        MannerRating mannerRating = mannerRatingRepository.findById(mannerId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_NOT_FOUND));

        // 매너평가 작성자가 맞는지 검증.
        if (!mannerRating.getFromMember().getId().equals(memberId)) {
            throw new MannerHandler(ErrorStatus.MANNER_UNAUTHORIZED);
        }

        // 매너평가
        if (mannerRating.getIsPositive()){

            // mannerKeyword 의 실제 존재 여부 검증 및 매너평가 키워드인지 검증.
            List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
            request.getMannerRatingKeywordList()
                    .forEach(mannerKeywordId -> {
                        MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                        if(!mannerKeyword.getIsPositive()){
                            throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
                        }
                        mannerRatingKeywordList.add(mannerKeyword);
                    });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long,MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList().stream()
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
            return mannerRatingRepository.save(mannerRating);
        }

        // 비매너 평가
        else {

            // mannerKeyword 의 실제 존재 여부 검증 및 비매너평가 키워드인지 검증.
            List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
            request.getMannerRatingKeywordList()
                    .forEach(mannerKeywordId -> {
                        MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                        if(mannerKeyword.getIsPositive()){
                            throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
                        }
                        mannerRatingKeywordList.add(mannerKeyword);
                    });

            // 기존 MannerRatingKeyword 엔티티 업데이트
            Map<Long,MannerRatingKeyword> existingMannerRatings = mannerRating.getMannerRatingKeywordList().stream()
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
            return mannerRatingRepository.save(mannerRating);
        }

    }
}
