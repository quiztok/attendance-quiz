package poly.service.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Service;

import poly.service.IWordAPIService;
import poly.service.comm.ICommCont;
import poly.service.comm.MongoDBComon;
import poly.util.DateUtil;
import poly.util.JsonReader;

@Service("WordAPIService")
public class WordAPIService extends MongoDBComon implements IWordAPIService, ICommCont {

	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public int attendQuizWord() throws Exception {

		log.info(this.getClass().getName() + ".attendQuizWord Start!");

		int res = 0;

		String colNm = "ATTEND_NN";
		JsonReader jr = new JsonReader();
		JSONArray json = jr.readJsonArrFromUrl(IWordAPIService.tagUrl);
		jr = null;

		// 출석태그
		List<Document> wordList = new ArrayList<Document>();

		json.stream().forEach(data -> wordList.add(Document.parse(data.toString())));

		wordList.forEach(data -> log.info("data : " + data));

		if (wordList.size() > 0) {

			super.DeleteCreateCollectionUniqueIndex(colNm, "id");

			// 분할 저장하기
			super.insertMany(colNm, wordList);

			res = 1;
		}

		log.info(this.getClass().getName() + ".attendQuizWord End!");

		return res;

	}

	@Override
	public int emergencyAttendQuizWord(String pWords) throws Exception {

		log.info(this.getClass().getName() + ".emergencyAttendQuizWord Start!");

		int res = 0;

		String[] words = pWords.split(",");

		int wordCnt = words.length;

		// 출석퀴즈 대상 단어의 수가 14가 아니면 에러
		if (wordCnt == 14) {

			List<Document> sList = new LinkedList<Document>();

			for (int i = 0; i < wordCnt; i++) {

				Document doc = new Document();
				doc.append("word_day", DateUtil.getDateTimeAdd(i));
				doc.append("word", words[i]);

				sList.add(doc);

				doc = null;
			}

			String colNm = "ATTEND_NN";

			super.DeleteCreateCollectionUniqueIndex(colNm, "word_day");

			// 분할 저장하기
			super.insertMany(colNm, sList);

			res = 1;

		}

		log.info(this.getClass().getName() + ".emergencyAttendQuizWord End!");

		return res;
	}

}
