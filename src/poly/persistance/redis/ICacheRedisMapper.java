package poly.persistance.redis;

import java.util.List;
import java.util.Map;

import org.bson.Document;

public interface ICacheRedisMapper {

	/*
	 * #############################################################################
	 * 인기 퀴즈
	 * #############################################################################
	 * //
	 */
//	public int insertRankResult(RankResultDTO pDTO, int rank) throws Exception;
//
//	public Set<RankResultDTO> getRankResult(int rank) throws Exception;
//
//	public int deleteRankResult() throws Exception;
//
//	public int setQuizRankExpire() throws Exception;

	/*
	 * #############################################################################
	 * 개인화 서비스
	 * #############################################################################
	 */

	// 연령대별 카테고리 키 값
	public String[] PersonalAgeKeyArray = { "AC", "10C", "10M", "10F", "20C", "20M", "20F", "30C", "30M", "30F", "40C",
			"40M", "40F", "50C", "50M", "50F", "60C", "60M", "60F" };

	// 연령대별 카테고리 저장
	public int savePersonalAge(List<Document> rList) throws Exception;

	// 연령대별 카테코리 가져오기
	public List<Object> getPersonalAge(String category) throws Exception;

	// 연령대별 전체 카테코리 가져오기
	public Map<String, List<Object>> getPersonalAgeAll() throws Exception;

	// 사용자별 개인화 추천 퀴즈 저장하기
	public int insertPersonalAnalysisForUser(String key, String user_id, Document qp_info) throws Exception;

	// 사용자별 개인화 추천 퀴즈 저장 시간 정의하기
	public int setTimeOutForPersonalAnalysis(String key) throws Exception;

	/*
	 * #############################################################################
	 * 오늘의 퀴즈
	 * #############################################################################
	 */

	// 오늘의 퀴즈 저장하기
	public int insertTodayQuiz(List<Map<String, Object>> pList) throws Exception;

	// 오늘의 퀴즈 가져오기
	public List<Map<String, Object>> getTodayQuiz() throws Exception;
}
