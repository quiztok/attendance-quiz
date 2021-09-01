package poly.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import poly.service.IAttendQuizService;
import poly.service.comm.ICommCont;
import poly.service.comm.MongoDBComon;
import poly.util.CmmUtil;
import poly.util.DateUtil;
import poly.util.NumberUtil;

@Service("AttendQuizService")
public class AttendQuizService extends MongoDBComon implements IAttendQuizService, ICommCont {

	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private MongoTemplate mongodb;

	/**
	 * 출석 퀴즈 대상 퀴즈아이디 가져오기
	 */
	private void doQuizIds(int pId, String pWord) throws Exception {

		String ContAnaysisStdDay = DateUtil.getDateTime("yyyyMMdd");

		String colNm = "NLP_QUIZLOG_DICTIONARY";

		MongoCollection<Document> col = null;
		FindIterable<Document> rs = null;
		Iterator<Document> cursor = null;

		col = mongodb.getCollection(colNm);

		Document query = new Document();

		query.append("nn", pWord);

		Document projection = new Document();

		projection.append("id", pId);
		projection.append("word", pWord);
		projection.append("quizId", "$quizId");
		projection.append("_id", 0);

		rs = col.find(query).projection(projection);
		cursor = rs.iterator();

		List<Document> sList = new ArrayList<Document>();

		int sSize = 0;

		while (cursor.hasNext()) {

			Document doc = cursor.next();
			String quizId = doc.getString("quizId");
			doc = null;

			Document pDoc = new Document();
			pDoc.append("id", pId);
			pDoc.append("word", pWord);
			pDoc.append("quizId", quizId);

			sList.add(pDoc);

			pDoc = null;

			sSize++;

		}

		log.info("pWord : " + pWord + " / sSize : " + sSize);

//		List<Document> rList = IteratorUtils.toList(cursor);

		cursor = null;
		rs = null;
		col = null;
		query = null;
		projection = null;

		colNm = "ATTEND_QUIZ_" + ContAnaysisStdDay + "_STEP1";

		// 데이터 저장하기
		col = mongodb.getCollection(colNm);
		col.insertMany(sList);
		col = null;

		sList = null;

	}

	/**
	 * 출석 퀴즈 대상 단어 가져오기
	 */
	private int doProcessStep1() throws Exception {
		log.info(this.getClass().getName() + ".doProcessStep1 Start!");

		int res = 0;

		String ContAnaysisStdDay = DateUtil.getDateTime("yyyyMMdd");

		String colNm = "ATTEND_QUIZ_" + ContAnaysisStdDay + "_STEP1";

		String[] idx = { "id" };

		super.DeleteCreateCollection(colNm, idx);

		// 컬렉션 이름
		colNm = "ATTEND_NN";

		MongoCollection<Document> col = null;
		FindIterable<Document> rs = null;
		Iterator<Document> cursor = null;

		col = mongodb.getCollection(colNm);

		Document projection = new Document();

		projection.append("tagName", "$tagName");
		projection.append("id", "$id");
		projection.append("_id", 0);

		rs = col.find(new Document()).projection(projection);
		cursor = rs.iterator();

		while (cursor.hasNext()) {
			Document doc = cursor.next();

			if (doc == null) {
				doc = new Document();

			}

			String tagName = CmmUtil.nvl(doc.getString("tagName"));
			int id = NumberUtil.getInt(doc.get("id"));

			this.doQuizIds(id, tagName);

//			rList.forEach(rDoc -> sList.add(new Document().append("id", id).append("tagName", tagName).append("quizId",
//					rDoc.getString("quizId"))));
//
//			sList.add(doc);

			doc = null;

		}

		cursor = null;
		rs = null;
		col = null;
		projection = null;

		res = 1;

		log.info(this.getClass().getName() + ".doProcessStep1 End!");

		return res;
	}

	/**
	 * 데이터 중복제거 및 데이터셋 구성
	 */
	private int doProcessStep2() throws Exception {
		log.info(this.getClass().getName() + ".doProcessStep2 Start!");

		int res = 0;

		String ContAnaysisStdDay = DateUtil.getDateTime("yyyyMMdd");

		Iterator<Document> cursor = null;
		AggregateIterable<Document> rs = null;

		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$group",
						new Document().append("_id", new Document().append("quiz_id", "$quiz_id"))
								.append("MAX(quiz_title)", new Document().append("$max", "$quiz_title"))
								.append("MAX(answerType)", new Document().append("$max", "$answerType"))
								.append("MAX(answerExplanation)", new Document().append("$max", "$answerExplanation"))
								.append("MAX(answerYN)", new Document().append("$max", "$answerYN"))
								.append("AVG(answerTrue)", new Document().append("$avg", "$answerTrue"))
								.append("AVG(answerFalse)", new Document().append("$avg", "$answerFalse"))
								.append("MAX(exampleText1)", new Document().append("$max", "$exampleText1"))
								.append("MAX(exampleText2)", new Document().append("$max", "$exampleText2"))
								.append("MAX(exampleText3)", new Document().append("$max", "$exampleText3"))
								.append("MAX(exampleText4)", new Document().append("$max", "$exampleText4"))),
				new Document().append("$project", new Document().append("quiz_id", "$_id.quiz_id")
						.append("quiz_title", "$MAX(quiz_title)").append("answerType", "$MAX(answerType)")
						.append("answerExplanation", "$MAX(answerExplanation)").append("answerYN", "$MAX(answerYN)")
						.append("answerTrue", "$AVG(answerTrue)").append("answerFalse", "$AVG(answerFalse)")
						.append("exampleText1", "$MAX(exampleText1)").append("exampleText2", "$MAX(exampleText2)")
						.append("exampleText3", "$MAX(exampleText3)").append("exampleText4", "$MAX(exampleText4)")
						.append("_id", 0)));

		String colNm = "QUIZLOG_DATASET_TYPE1_" + ContAnaysisStdDay + "_STEP1";

		MongoCollection<Document> col = mongodb.getCollection(colNm);

		rs = col.aggregate(pipeline).allowDiskUse(true);
		cursor = rs.iterator();

		// 컬렉션 데이터를 List 형태로 변환
		List<Document> rList = IteratorUtils.toList(cursor);

		cursor = null;
		rs = null;
		col = null;

		colNm = "QUIZLOG_DATASET_TYPE1_" + ContAnaysisStdDay;

		super.DeleteCreateCollectionUniqueIndex(colNm, "quiz_id");

		col = mongodb.getCollection(colNm);
		col.insertMany(rList);

		col = null;
		rList = null;

		log.info("#################################################################");
		log.info("# doProcessStep2 Result!!");
		log.info("# " + colNm + " insert Doc Count : " + mongodb.getCollection(colNm).countDocuments());
		log.info("#################################################################");

		res = 1;

		log.info(this.getClass().getName() + ".doProcessStep2 End!");

		return res;

	}

	/**
	 * 사용 완료된 임시 컬렉션 삭제
	 */
	private int doClean() throws Exception {
		log.info(this.getClass().getName() + ".doClean Start!");

		int res = 0;

		// 오늘 날짜
		String curDay = DateUtil.getDateTime("yyyyMMdd");

		// 어제 날짜
		String preDay = DateUtil.getDateTimeAdd(-1);

		// 퀴즈 풀이 데이터 삭제
		String colNm = "QUIZLOG_DATASET_TYPE1_" + curDay + "_STEP1";

		if (mongodb.collectionExists(colNm)) {
			mongodb.dropCollection(colNm);
			log.info("Drop Collection : " + colNm);

		}

		// 이력 테이블 삭제
		colNm = "QUIZLOG_DATASET_TYPE1_" + preDay;

		if (mongodb.collectionExists(colNm)) {
			mongodb.dropCollection(colNm);
			log.info("Drop Collection : " + colNm);

		}

		res = 1;

		log.info(this.getClass().getName() + ".doClean End!");

		return res;
	}

	@Override
	public int doDataAnalysis() throws Exception {

		int res = 0;

		if (this.doProcessStep1() != 1) {
			return 0;
		}

//		if (this.doProcessStep2() != 1) {
//			return 0;
//		}

//		// 임시 컬렉션 삭제
//		if (this.doClean() != 1) {
//			return 0;
//		}

		res = 1;

		return res;
	}

}
