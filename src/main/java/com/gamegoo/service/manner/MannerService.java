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
        List<MannerRating> mannerRatings = mannerRatingRepository.findByToMemberId(targetMember.getId());
        if (!mannerRatings.isEmpty()){
            for (MannerRating mannerRating : mannerRatings) {
                if (mannerRating.getFromMember().getId().equals(member.getId())){
                    List<MannerRatingKeyword> mannerKeywords = mannerRatingKeywordRepository.findByMannerRating(mannerRating);
                    if (!mannerKeywords.isEmpty()){
                        MannerRatingKeyword mannerKeyword = mannerKeywords.get(0);  // 첫번째 키워드만 조회.
                        if (mannerKeyword.getMannerKeyword().getIsPositive()){
                            throw new MannerHandler(ErrorStatus.MANNER_CONFLICT);
                        }
                    }
                }
            }
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    mannerRatingKeywordList.add(mannerKeyword);
                });

        // mannerKeyword 유형 검증.
        for (Long keyword : request.getMannerRatingKeywordList()){
            if (keyword < 1 || keyword > 6) {
                throw new MannerHandler(ErrorStatus.MANNER_KEYWORD_TYPE_INVALID);
            }
        }

        // manner rating 엔티티 생성 및 연관관계 매핑.
        MannerRating mannerRating = MannerRating.builder()
                .fromMember(member)
                .mannerRatingKeywordList(new ArrayList<>())
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
        List<MannerRating> mannerRatings = mannerRatingRepository.findByToMemberId(targetMember.getId());
        if (!mannerRatings.isEmpty()){
            for (MannerRating mannerRating : mannerRatings) {
                if (mannerRating.getFromMember().getId().equals(member.getId())){
                    List<MannerRatingKeyword> mannerKeywords = mannerRatingKeywordRepository.findByMannerRating(mannerRating);
                    if (!mannerKeywords.isEmpty()){
                        MannerRatingKeyword mannerKeyword = mannerKeywords.get(0);  // 첫번째 키워드만 조회.
                        if (!mannerKeyword.getMannerKeyword().getIsPositive()){
                            throw new MannerHandler(ErrorStatus.BAD_MANNER_CONFLICT);
                        }
                    }
                }
            }
        }

        // mannerKeyword 의 실제 존재 여부 검증.
        List<MannerKeyword> mannerRatingKeywordList = new ArrayList<>();
        request.getMannerRatingKeywordList()
                .forEach(mannerKeywordId -> {
                    MannerKeyword mannerKeyword = mannerKeywordRepository.findById(mannerKeywordId).orElseThrow(() -> new MannerHandler(ErrorStatus.MANNER_KEYWORD_NOT_FOUND));
                    mannerRatingKeywordList.add(mannerKeyword);
                });

        // mannerKeyword 유형 검증.
        for (Long keyword : request.getMannerRatingKeywordList()){
            if (keyword < 7 || keyword > 12) {
                throw new MannerHandler(ErrorStatus.BAD_MANNER_KEYWORD_TYPE_INVALID);
            }
        }

        // manner rating 엔티티 생성 및 연관관계 매핑.
        MannerRating mannerRating = MannerRating.builder()
                .fromMember(member)
                .mannerRatingKeywordList(new ArrayList<>())
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
}
