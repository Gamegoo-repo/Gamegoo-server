package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.champion.Champion;
import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.dto.member.RiotResponse;
import com.gamegoo.repository.member.ChampionRepository;
import com.gamegoo.repository.member.MemberChampionRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RiotService {
    private final MemberRepository memberRepository;
    private final ChampionRepository championRepository;
    private final MemberChampionRepository memberChampionRepository;
    private final RestTemplate restTemplate;
    @Value("${riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_ACCOUNT_API_URL_TEMPLATE = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String RIOT_SUMMONER_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s";
    private static final String RIOT_LEAGUE_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/%s?api_key=%s";
    private static final String RIOT_MATCH_API_URL_TEMPLATE = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=20&api_key=%s";
    private static final String RIOT_MATCH_INFO_API_URL_TEMPLATE = "https://asia.api.riotgames.com/lol/match/v5/matches/%s?api_key=%s";


    // Riot GameName으로 DB에 데이터 저장하기
    public void updateMemberRiotInfo(String gameName, String tag, String email) {
        // emaiL 로 DB에서 member 가져오기
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 해당 Request에 있는 Email이 이미 DB의 다른 계정과 연동되었을 경우
        if (member.getGameuserName() != null) {
            throw new MemberHandler(ErrorStatus.RIOT_MEMBER_CONFLICT);
        }

        // 해당 Request에 있는 GameName이 이미 DB의 다른 email과 연동되었을 경우
        if (memberRepository.findByGameuserName(gameName).isPresent()) {
            throw new MemberHandler(ErrorStatus.RIOT_ACCOUNT_CONFLICT);
        }


        /* 티어, 랭킹 정보 불러오기 */
        // 1. game_name, tag로 사용자 puuid 얻기
        String puuid = getRiotPuuid(gameName, tag);
        // 2. puuid를 통해 encryptedsummonerid 얻기
        String encryptedSummonerId = getSummonerId(puuid);
        // 3. tier, rank 정보 DB에 저장하기
        updateMemberWithLeagueInfo(member, gameName, encryptedSummonerId);
        memberRepository.save(member);
        
        /* 최근 사용한 챔피언 3개 찾기 */
        // 1. riot API에서 최근 매칭 ID 10 개 List에 저장
        List<String> recentMatchIds = getRecentMatchIds(puuid);

        // 2. List에 있는 매칭 ID 바탕으로 각 매칭에서 유저가 사용한 캐릭터 불러오기
        List<Integer> recentChampionIds = recentMatchIds.stream()
                .map(matchId -> getChampionIdFromMatch(matchId, gameName))
                .toList();

        // 3. 해당 캐릭터 중 많이 사용한 캐릭터 세 개 저장하기
        //      (1) 챔피언 사용 빈도 계산
        Map<Integer, Long> championFrequency = recentChampionIds.stream()
                .collect(Collectors.groupingBy(championId -> championId, Collectors.counting()));

        //      (2) 빈도를 기준으로 정렬하여 상위 3개의 챔피언 추출
        List<Integer> top3Champions = championFrequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 3. 캐릭터와 유저 데이터 매핑해서 DB에 저장하기
        //    (1) 해당 email을 가진 사용자의 정보가 MemberChampion 테이블에 있을 경우 제거
        member.getMemberChampionList()
                .forEach(memberChampion -> {
                    memberChampion.removeMember(member); // 양방향 연관관계 제거
                    memberChampionRepository.delete(memberChampion);
                });

        //    (2) Champion id, Member id 엮어서 MemberChampion 테이블에 넣기
        top3Champions
                .forEach(championId -> {
                    Champion champion = championRepository.findById(Long.valueOf(championId))
                            .orElseThrow(() -> new MemberHandler(ErrorStatus.CHAMPION_NOT_FOUND));

                    MemberChampion memberChampion = MemberChampion.builder()
                            .member(member)
                            .champion(champion)
                            .build();

                    memberChampionRepository.save(memberChampion);
                });

    }

    // RiotAPI - request:game_name, tag / response : puuid
    private String getRiotPuuid(String game_name, String tag) {
        String url = String.format(RIOT_ACCOUNT_API_URL_TEMPLATE, game_name, tag, riotAPIKey);
        RiotResponse.RiotAccountDTO accountResponse = null;
        try {
            accountResponse = restTemplate.getForObject(url, RiotResponse.RiotAccountDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
            }
            throw e;
        }

        assert accountResponse != null;
        return accountResponse.getPuuid();

    }

    // RiotAPI - request:puuid / response : encryptedSummonerId
    private String getSummonerId(String puuid) {

        String summonerUrl = String.format(RIOT_SUMMONER_API_URL_TEMPLATE, puuid, riotAPIKey);
        RiotResponse.RiotSummonerDTO summonerResponse = restTemplate.getForObject(summonerUrl, RiotResponse.RiotSummonerDTO.class);

        if (summonerResponse == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        return summonerResponse.getId();
    }

    // RiotAPI - request: encryptedSummonerId / response : tier, rank
    private void updateMemberWithLeagueInfo(Member member, String gameName, String encryptedSummonerId) {
        // 3. account id로 티어, 랭크, 불러오기
        String leagueUrl = String.format(RIOT_LEAGUE_API_URL_TEMPLATE, encryptedSummonerId, riotAPIKey);
        RiotResponse.RiotLeagueEntryDTO[] leagueEntries = restTemplate.getForObject(leagueUrl, RiotResponse.RiotLeagueEntryDTO[].class);

        if (leagueEntries == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        for (RiotResponse.RiotLeagueEntryDTO entry : leagueEntries) {
            // 솔랭일 경우에만 저장
            if ("RANKED_SOLO_5x5".equals(entry.getQueueType())) {
                int wins = entry.getWins();
                int losses = entry.getLosses();
                double winrate = (double) wins / (wins + losses);
                winrate = Math.round(winrate * 1000) / 10.0;

                // DB에 저장
                member.setTier(entry.getTier());
                member.setRank(entry.getRank());
                member.setWinRate(winrate);
                break;
            }
        }

        // 솔랭을 하지 않는 유저는 gameName만 저장
        member.setGameuserName(gameName);
    }

    // RiotAPI - request: puuid / response : matchId
    private List<String> getRecentMatchIds(String puuid) {
        // 최근 매칭 ID 3개 가져오기
        String matchUrl = String.format(RIOT_MATCH_API_URL_TEMPLATE, puuid, riotAPIKey);
        String[] matchIds = restTemplate.getForObject(matchUrl, String[].class);

        if (matchIds == null || matchIds.length == 0) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        return Arrays.asList(matchIds);
    }

    // RiotAPI - request: matchId / response : championId
    private Integer getChampionIdFromMatch(String matchId, String game_name) {
        // 매치 정보 가져오기
        String matchInfoUrl = String.format(RIOT_MATCH_INFO_API_URL_TEMPLATE, matchId, riotAPIKey);
        RiotResponse.MatchDTO matchResponse = restTemplate.getForObject(matchInfoUrl, RiotResponse.MatchDTO.class);

        if (matchResponse == null || matchResponse.getInfo() == null || matchResponse.getInfo().getParticipants() == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        // 참가자 정보에서 game_name과 일치하는 사용자의 champion ID 찾기
        return matchResponse.getInfo().getParticipants().stream()
                .filter(participant -> game_name.equals(participant.getRiotIdGameName()))
                .map(RiotResponse.ParticipantDTO::getChampionId)
                .findFirst()
                .orElseThrow(() -> new MemberHandler(ErrorStatus.RIOT_NOT_FOUND));
    }


}
