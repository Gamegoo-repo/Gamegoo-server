package com.gamegoo.util;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.member.Tier;
import com.gamegoo.dto.member.RiotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RiotUtil {
    private final RestTemplate restTemplate;
    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_ACCOUNT_API_URL_TEMPLATE = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String RIOT_SUMMONER_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s";
    private static final String RIOT_LEAGUE_API_URL_TEMPLATE = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/%s?api_key=%s";
    private static final String RIOT_MATCH_API_URL_TEMPLATE = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%s&api_key=%s";
    private static final String RIOT_MATCH_INFO_API_URL_TEMPLATE = "https://asia.api.riotgames.com/lol/match/v5/matches/%s?api_key=%s";

    @Autowired
    public RiotUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Riot API : 최근 선호 챔피언 3개 리스트 조회
     *
     * @param gameName
     * @param puuid
     * @return
     */
    @Transactional
    public List<Integer> getPreferChampionfromMatch(String gameName, String puuid) {
        // 1. 최근 플레이한 챔피언 리스트 조회
        List<Integer> recentChampionIds = null;
        int count = 20;

        try {
            while ((recentChampionIds == null || recentChampionIds.size() < 3) && count <= 100) {
                List<String> recentMatchIds = getRecentMatchIds(puuid, count);

                recentChampionIds = recentMatchIds.stream()
                        .map(matchId -> getChampionIdFromMatch(matchId, gameName))
                        .filter(championId -> championId < 1000)
                        .toList();

                if (recentChampionIds.size() < 3) {
                    count += 10; // count를 10 증가시켜서 다시 시도
                }
            }
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus.RIOT_MATCH_NOT_FOUND);
        }

        // 최근 선호 챔피언 수가 충분하지 않을 경우 에러 발생
        if (recentChampionIds.size() < 3) {
            throw new MemberHandler(ErrorStatus.RIOT_INSUFFICIENT_MATCHES);
        }

        // 2. 해당 캐릭터 중 많이 사용한 캐릭터 세 개 저장하기
        //      (1) 챔피언 사용 빈도 계산
        Map<Integer, Long> championFrequency = recentChampionIds.stream()
                .collect(Collectors.groupingBy(championId -> championId, Collectors.counting()));

        //      (2) 빈도를 기준으로 정렬하여 상위 3개의 챔피언 추출
        List<Integer> top3Champions = championFrequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        if (top3Champions.isEmpty()) {
            throw new MemberHandler(ErrorStatus.RIOT_INSUFFICIENT_MATCHES);
        }

        return top3Champions;
    }

    /**
     * RiotAPI : gameName, tag으로 puuid 조회
     *
     * @param gameName
     * @param tag
     * @return
     */
    public String getRiotPuuid(String gameName, String tag) {
        String url = String.format(RIOT_ACCOUNT_API_URL_TEMPLATE, gameName, tag, riotAPIKey);
        RiotResponse.RiotAccountDTO accountResponse = null;
        try {
            accountResponse = restTemplate.getForObject(url, RiotResponse.RiotAccountDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
            }
            throw new MemberHandler(ErrorStatus.RIOT_ERROR);
        }

        if (accountResponse == null) {
            throw new MemberHandler(ErrorStatus.RIOT_ERROR);
        }

        return accountResponse.getPuuid();
    }

    /**
     * RiotAPI : puuid로 encryptedSummonerId 조회
     *
     * @param puuid
     * @return
     */
    public String getSummonerId(String puuid) {

        String summonerUrl = String.format(RIOT_SUMMONER_API_URL_TEMPLATE, puuid, riotAPIKey);
        RiotResponse.RiotSummonerDTO summonerResponse = null;
        try {
            summonerResponse = restTemplate.getForObject(summonerUrl, RiotResponse.RiotSummonerDTO.class);

            if (summonerResponse == null) {
                throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus.RIOT_ERROR);
        }

        return summonerResponse.getId();
    }

    /**
     * RiotAPI : encryptedSummonerId로 tier, rank, winrate 조회
     *
     * @param member
     * @param gameName
     * @param encryptedSummonerId
     * @param tag
     */
    public void addTierRankWinRate(Member member, String gameName, String encryptedSummonerId, String tag) {
        // account id로 티어, 랭크, 불러오기
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
                Tier tier = Tier.valueOf(entry.getTier().toUpperCase());

                // DB에 저장
                member.updateRiotDetails(tier, entry.getRank(), winrate);
                break;
            }
        }

        // 솔랭을 하지 않는 유저는 gameName만 저장
        member.updateRiotBasic(gameName, tag);
    }

    /**
     * RiotAPI : puuid로 최근 매칭 20개의 matchId 가져오기
     *
     * @param puuid
     * @param count
     * @return
     */
    private List<String> getRecentMatchIds(String puuid, int count) {
        // 최근 매칭 ID 20개 가져오기
        String matchUrl = String.format(RIOT_MATCH_API_URL_TEMPLATE, puuid, count, riotAPIKey);
        String[] matchIds = restTemplate.getForObject(matchUrl, String[].class);

        if (matchIds == null || matchIds.length == 0) {
            throw new MemberHandler(ErrorStatus.RIOT_MATCH_NOT_FOUND);
        }

        return Arrays.asList(matchIds);
    }


    /**
     * RiotAPI : matchId로 선호 챔피언 데이터 조회
     *
     * @param matchId
     * @param gameName
     * @return
     */
    public Integer getChampionIdFromMatch(String matchId, String gameName) {
        // 매치 정보 가져오기
        String matchInfoUrl = String.format(RIOT_MATCH_INFO_API_URL_TEMPLATE, matchId, riotAPIKey);
        RiotResponse.MatchDTO matchResponse = restTemplate.getForObject(matchInfoUrl, RiotResponse.MatchDTO.class);

        if (matchResponse == null || matchResponse.getInfo() == null || matchResponse.getInfo().getParticipants() == null) {
            throw new MemberHandler(ErrorStatus.RIOT_NOT_FOUND);
        }

        // 참가자 정보에서 gameName과 일치하는 사용자의 champion ID 찾기
        return matchResponse.getInfo().getParticipants().stream()
                .filter(participant -> gameName.equals(participant.getRiotIdGameName()))
                .map(RiotResponse.ParticipantDTO::getChampionId)
                .findFirst()
                .orElseThrow(() -> new MemberHandler(ErrorStatus.RIOT_MATCH_NOT_FOUND));
    }

}
