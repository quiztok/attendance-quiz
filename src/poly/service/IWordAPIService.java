package poly.service;

public interface IWordAPIService {

	// 출석퀴즈 태그 호출 API
	public String tagUrl = "https://channel-kr.quiztok.com/tag/polytec/filter/today-quizpack";

	/**
	 * 출석퀴즈 추출을 위한 일반적인 API
	 */
	int attendQuizWord() throws Exception;

	/**
	 * 긴급 변경을 위한 출석퀴즈 추출을 위한 일반적인 API
	 */
	int emergencyAttendQuizWord(String pWords) throws Exception;
}
