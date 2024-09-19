package com.gamegoo.service.matching;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MatchingHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.matching.MatchingRecord;
import com.gamegoo.domain.matching.MatchingStatus;
import com.gamegoo.domain.matching.MatchingType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.member.Tier;
import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.dto.matching.MatchingResponse.matchingRequestResponseDTO;
import com.gamegoo.dto.matching.MemberPriority;
import com.gamegoo.repository.matching.MatchingRecordRepository;
import com.gamegoo.repository.member.BlockRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.member.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MemberRepository memberRepository;
    private final MatchingRecordRepository matchingRecordRepository;
    private final BlockRepository blockRepository;
    private final ProfileService profileService;

    /**
     * 우선순위 계산
     *
     * @param request
     * @param id
     * @return
     * @throws MemberHandler
     */
    public MatchingResponse.PriorityMatchingResponseDTO getPriorityLists(
            MatchingRequest.InitializingMatchingRequestDTO request, Long id) throws MemberHandler {


        // 게임 모드가 같고, 5분동안 매칭이 되지 않은 매칭 기록 가져오기
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<MatchingRecord> matchingRecords = matchingRecordRepository.findTopByCreatedAtAfterAndStatusAndGameModeGroupByMemberId(
                fiveMinutesAgo, MatchingStatus.PENDING, request.getGameMode());

        Member member = profileService.findMember(id);

        List<MemberPriority> otherPriorityList = new ArrayList<>();
        List<MemberPriority> myPriorityList = new ArrayList<>();


        MatchingRecord myMatchingRecord = MatchingRecord.builder()
                .member(member)
                .mike(request.getMike())
                .rank(member.getRank())
                .tier(member.getTier())
                .winRate(member.getWinRate())
                .status(MatchingStatus.PENDING)
                .matchingType(MatchingType.valueOf(request.getMatchingType()))
                .mainPosition(request.getMainP())
                .subPosition(request.getSubP())
                .wantPosition(request.getWantP())
                .mannerLevel(member.getMannerLevel())
                .gameMode(request.getGameMode())
                .build();

        // 우선순위 계산하기
        for (MatchingRecord record : matchingRecords) {
            Long otherMemberId = record.getMember().getId();

            // 서로 차단한 사용자일 경우 우선순위 계산 X
            if (blockRepository.existsByBlockerMemberAndBlockedMember(member, record.getMember())) {
                continue;
            }

            if (!id.equals(otherMemberId)) {
                int otherPriority = calculatePriority(myMatchingRecord, record);
                myPriorityList.add(new MemberPriority(otherMemberId, otherPriority));

                int myPriority = calculatePriority(record, myMatchingRecord);
                otherPriorityList.add(new MemberPriority(record.getMember().getId(), myPriority));
            }


        }

        // 내 매칭 기록 dto 생성
        MatchingResponse.matchingRequestResponseDTO myMatchingInfo = matchingRequestResponseDTO.builder()
                .memberId(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(member.getRank())
                .mannerLevel(member.getMannerLevel())
                .profileImg(member.getProfileImage())
                .gameMode(request.getGameMode())
                .mainPosition(request.getMainP())
                .subPosition(request.getSubP())
                .wantPosition(request.getWantP())
                .mike(request.getMike())
                .gameStyleList(profileService.getGameStyleList(member))
                .build();

        return MatchingResponse.PriorityMatchingResponseDTO.builder()
                .myPriorityList(myPriorityList)
                .otherPriorityList(otherPriorityList)
                .myMatchingInfo(myMatchingInfo)
                .build();
    }


    /**
     * 우선순위 계산
     *
     * @param myMatchingRecord
     * @param otherMatchingRecord
     * @return
     */
    private int calculatePriority(MatchingRecord myMatchingRecord,
                                  MatchingRecord otherMatchingRecord) {
        int priority = 0;

        Integer myRank = myMatchingRecord.getRank();
        Tier myTier = myMatchingRecord.getTier();
        Integer myManner = myMatchingRecord.getMannerLevel();
        Integer myMainPosition = myMatchingRecord.getMainPosition();
        Integer mySubPosition = myMatchingRecord.getSubPosition();
        Integer myWantPosition = myMatchingRecord.getWantPosition();
        Integer gameMode = myMatchingRecord.getGameMode();
        MatchingType myMatchingType = myMatchingRecord.getMatchingType();
        Boolean myMike = myMatchingRecord.getMike();

        Integer otherRank = otherMatchingRecord.getMember().getRank();
        Tier otherTier = otherMatchingRecord.getMember().getTier();
        Integer otherMainPosition = otherMatchingRecord.getMainPosition();
        Integer otherSubPosition = otherMatchingRecord.getSubPosition();
        Integer otherWantPosition = otherMatchingRecord.getWantPosition();
        Boolean otherMike = otherMatchingRecord.getMike();
        Integer otherManner = otherMatchingRecord.getMannerLevel();
        Integer otherGameMode = otherMatchingRecord.getGameMode();

        // 게임 모드가 다를 경우 X (find로 한 번 걸렀지만 혹시 모르기때문에 예외처리)
        if (!gameMode.equals(otherGameMode)) {
            return 0;
        }

        // 주/부 포지션 조합이 같을 경우 X
        // 겹치는게 둘 중 하나라도 0일 경우는 X
        if ((otherMainPosition.equals(myMainPosition) && otherSubPosition.equals(mySubPosition) && !(otherMainPosition.equals(0) || otherSubPosition.equals(0))) ||
                (otherMainPosition.equals(mySubPosition) && otherSubPosition.equals(myMainPosition) && !(otherMainPosition.equals(0) || otherSubPosition.equals(0)))) {
            return 0;
        }

        // 개인 랭크 예외조건
        if (gameMode == 2) {
            // 티어 차이가 1개 이상 나면 X
            if (Math.abs(myTier.ordinal() - otherTier.ordinal()) > 1) {
                return 0;
            }

            // 마스터 이상은 게임 불가능
            if (myTier.ordinal() >= 7 || otherTier.ordinal() >= 7) {
                return 0;
            }
        }

        // 자유랭크 예외조건
        if (gameMode == 3) {
            // 마스터 이상 플레이어는 골드 이하의 플레이어와 매칭 X
            if (myTier.ordinal() >= 7 && otherTier.ordinal() <= 3) {
                return 0;
            }
            if (otherTier.ordinal() >= 7 && myTier.ordinal() <= 3) {
                return 0;
            }
        }

        /**
         * <정밀매칭>
         * 차이가 적을수록 높은 우선순위를 갖도록 함
         *
         * 우선순위 : 1. 매너레벨 2. 랭크
         * 조건
         *      1. 마이크는 무조건 맞아야함
         *      2. 내가 원하는 포지션이 상대 포지션이어야함
         *      3. 티어 차이가 1개 이상 나면 X
         *
         * Ex) 랭크 : 36, 매너레벨 : 16 (52) vs 랭크 : 40, 매너레벨 : 12 (52)
         *        => (랭크가 4 이상 차이나야 매너레벨 1단계 다른 것과 같은 가중치를 가짐)
         *        => (골드 1과 실버 4는 랭크가 7 차이남)
         *
         */
        if (myMatchingType.equals(MatchingType.PRECISE)) {
            // 마이크가 다를 경우 우선순위 0
            if (!otherMike.equals(myMike)) {
                return 0;
            }

            // 내가 원하는 포지션이 상대 포지션이 아닐 경우 return 0
            if (!otherMainPosition.equals(myWantPosition) && !otherSubPosition.equals(
                    myWantPosition)) {
                return 0;
            }

            // 티어 차이가 1개 이상 나면 X
            if (Math.abs(myTier.ordinal() - otherTier.ordinal()) > 1) {
                return 0;
            }
        }

        /**
         * <겜구매칭>
         * 차이가 적을수록 높은 우선순위를 갖도록 함
         *
         * 우선순위 : 1. 매너레벨 2. 랭크, 마이크, 포지션 (개인랭크의 경우 랭크를 더 우선)
         */
        if (myMatchingType.equals(MatchingType.BASIC)) {
            // 마이크 가중치
            if (otherMike.equals(myMike)) {
                // 개인 랭크일 경우 랭크가 맞는게 더 중요함
                if (gameMode == 2) {
                    priority += 2;
                }
                priority += 3;
            }

            // 포지션 가중치
            // 칼바람 : 포지션 선택 제외하기 -> 포지션 점수 기본값으로 주고 시작하기
            if (gameMode == 4) {
                priority += 6;
            } else {
                if (myWantPosition.equals(otherMainPosition) || myWantPosition.equals(0)
                        || otherMainPosition.equals(0)) {
                    priority += 3;
                } else if (myWantPosition.equals(otherSubPosition) || otherSubPosition.equals(0)) {
                    priority += 2;
                } else {
                    priority += 1;
                }

                if (otherWantPosition.equals(myMainPosition) || otherWantPosition.equals(0)
                        || myMainPosition.equals(0)) {
                    priority += 3;
                } else if (otherWantPosition.equals(mySubPosition) || mySubPosition.equals(0)) {
                    priority += 2;
                } else {
                    priority += 1;
                }
            }
        }

        // 랭킹 가중치
        priority += getTierPriority(myTier, myRank, otherTier, otherRank);

        //매너레벨 가중치
        priority += getMannerPriority(otherManner, myManner);

        return Math.max(priority, 0); // 우선순위가 0보다 작아지지 않도록 조정
    }

    /**
     * <매너레벨 가중치> 최대 가중치 12 , 최소 가중치 0 (매너레벨 5, 매너레벨 1 -> (5-1)*3 = 4*3 = 12)
     */
    private int getMannerPriority(Integer otherManner, Integer myManner) {
        int priority = 0;
        int mannerDifference = Math.abs(myManner - otherManner);
        priority += 12 - mannerDifference * 3;
        return priority;
    }

    /**
     * <랭킹 가중치> 최대 가중치 40, 최소 가중치 1 (챌린저 1 - 아이언 4 = (9*4+(4-1)) - (0*4)+(4-4) = 39) 티어 차이가 너무 심해서
     * 사실상 30점 밑일 경우 매칭 안되는게 더 좋음
     *
     * @param myTier
     * @param myRank
     * @param otherTier
     * @param otherRank
     * @return 랭킹 우선순위 값
     */
    private int getTierPriority(Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {

        int priority = 0;

        // priority 값 계산 : 랭크
        int myScore = getTierRankScore(myTier, myRank);
        int otherScore = getTierRankScore(otherTier, otherRank);
        int scoreDifference = Math.abs(myScore - otherScore);

        priority += 40 - scoreDifference;
        return priority;
    }

    /**
     * 티어, 랭크 계산
     *
     * @param tier
     * @param rank
     * @return 랭킹 점수
     */
    private int getTierRankScore(Tier tier, int rank) {
        // 모든 티어는 1~4의 랭크가 있음
        // ex) 골드 4 : 4*4 + (4-4) = 16, 골드 3 : 4*4 + (4-3) = 17, 실버 1 : 3*4 + (4-1) = 15
        return tier.ordinal() * 4 + (4 - rank);
    }

    /**
     * 매칭 정보 저장
     *
     * @param request
     * @param id
     */
    @Transactional
    public void save(MatchingRequest.InitializingMatchingRequestDTO request, Long id) {
        // 회원 정보 불러오기
        Member member = profileService.findMember(id);

        // 매칭 기록 저장
        MatchingRecord matchingRecord = MatchingRecord.builder()
                .mike(request.getMike())
                .tier(member.getTier())
                .rank(member.getRank())
                .matchingType(MatchingType.valueOf(request.getMatchingType()))
                .status(MatchingStatus.PENDING)
                .mainPosition(request.getMainP())
                .subPosition(request.getSubP())
                .wantPosition(request.getWantP())
                .winRate(member.getWinRate())
                .gameMode(request.getGameMode())
                .mannerLevel(member.getMannerLevel())
                .member(member)
                .build();

        // 매칭 기록에 따라 member 정보 변경하기
        if (request.getMainP() != null && request.getSubP() != null && request.getWantP() != null) {
            member.updateMemberFromMatching(request.getMainP(), request.getSubP(), request.getMike());
        }
        if (request.getGameStyleIdList() != null) {
            profileService.addMemberGameStyles(request.getGameStyleIdList(), member.getId());
        }

        matchingRecordRepository.save(matchingRecord);
        memberRepository.save(member);
    }

    /**
     * 매칭 상태(status) 수정 : 회원의 가장 최신 매칭만 상태 변경 가능
     *
     * @param request
     * @param id
     */
    @Transactional
    public void updateMyStatus(MatchingRequest.ModifyMatchingRequestDTO request, Long id) {
        // 회원 정보 불러오기
        Member member = profileService.findMember(id);

        // 매칭 기록 불러오기
        MatchingRecord matchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        member, request.getGameMode())
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));

        try {
            MatchingStatus status = MatchingStatus.valueOf(request.getStatus().toUpperCase());
            // status 값 변경
            matchingRecord.updateStatus(status);
            matchingRecordRepository.save(matchingRecord);
        } catch (IllegalArgumentException e) {
            // status 값이 이상할 경우 에러처리
            throw new MatchingHandler(ErrorStatus.MATCHING_STATUS_BAD_REQUEST);
        }
    }

    /**
     * 나와 targetMember의 가장 최신 매칭 기록의 상태 변경
     *
     * @param request
     * @param memberId
     * @param targetMemberId
     */
    @Transactional
    public void updateBothStatus(MatchingRequest.ModifyMatchingRequestDTO request, Long memberId,
                                 Long targetMemberId) {

        // request 값 검증
        MatchingStatus status;
        try {
            status = MatchingStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            // status 값이 이상할 경우 에러처리
            throw new MatchingHandler(ErrorStatus.MATCHING_STATUS_BAD_REQUEST);
        }

        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

        // member의 매칭 기록 상태 변경
        MatchingRecord matchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        member, request.getGameMode())
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        matchingRecord.updateStatus(status);

        // targetMember의 매칭 기록 상태 변경
        MatchingRecord targetMatchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        targetMember, request.getGameMode())
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        targetMatchingRecord.updateStatus(status);
    }

    /**
     * @param memberId
     * @param targetMemberId
     * @return
     */
    @Transactional
    public MatchingResponse.matchingFoundResponseDTO foundMatching(Long memberId,
                                                                   Long targetMemberId, int gameMode) {

        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

        // member의 매칭 기록 상태 변경
        MatchingRecord matchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        member, gameMode)
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        matchingRecord.updateStatus(MatchingStatus.FOUND);

        // targetMember의 매칭 기록 상태 변경
        MatchingRecord targetMatchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        targetMember, gameMode)
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        targetMatchingRecord.updateStatus(MatchingStatus.FOUND);

        // response dto 생성
        MatchingResponse.matchingRequestResponseDTO myMatchingInfo = matchingRequestResponseDTO.builder()
                .memberId(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(matchingRecord.getRank())
                .mannerLevel(matchingRecord.getMannerLevel())
                .profileImg(member.getProfileImage())
                .gameMode(matchingRecord.getGameMode())
                .mainPosition(matchingRecord.getMainPosition())
                .subPosition(matchingRecord.getSubPosition())
                .wantPosition(matchingRecord.getWantPosition())
                .mike(matchingRecord.getMike())
                .gameStyleList(profileService.getGameStyleList(member))
                .build();

        MatchingResponse.matchingRequestResponseDTO targetMatchingInfo = matchingRequestResponseDTO.builder()
                .memberId(targetMember.getId())
                .gameName(targetMember.getGameName())
                .tag(targetMember.getTag())
                .tier(targetMember.getTier())
                .rank(targetMatchingRecord.getRank())
                .mannerLevel(targetMatchingRecord.getMannerLevel())
                .profileImg(targetMember.getProfileImage())
                .gameMode(targetMatchingRecord.getGameMode())
                .mainPosition(targetMatchingRecord.getMainPosition())
                .subPosition(targetMatchingRecord.getSubPosition())
                .wantPosition(targetMatchingRecord.getWantPosition())
                .mike(targetMatchingRecord.getMike())
                .gameStyleList(profileService.getGameStyleList(targetMember))
                .build();

        return MatchingResponse.matchingFoundResponseDTO.builder()
                .myMatchingInfo(myMatchingInfo)
                .targetMatchingInfo(targetMatchingInfo)
                .build();

    }

    /**
     * member와 targetMember의 매칭 기록을 SUCCESS 상태로 변경
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    @Transactional
    public void successMatching(Long memberId, Long targetMemberId, Integer gameMode) {
        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

        // member의 매칭 기록 상태 변경
        MatchingRecord matchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        member, gameMode)
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        matchingRecord.updateStatus(MatchingStatus.SUCCESS);

        // targetMember의 매칭 기록 상태 변경
        MatchingRecord targetMatchingRecord = matchingRecordRepository.findFirstByMemberAndGameModeOrderByUpdatedAtDesc(
                        targetMember, gameMode)
                .orElseThrow(() -> new MatchingHandler(ErrorStatus.MATCHING_NOT_FOUND));
        targetMatchingRecord.updateStatus(MatchingStatus.SUCCESS);
    }
}


