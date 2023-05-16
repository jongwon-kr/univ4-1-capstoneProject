package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DB_Conn {
	Connection con = null;

	public DB_Conn() {
		String url = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
		String id = "SYSTEM";
		String password = "1234";
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 적재 성공");
			con = DriverManager.getConnection(url, id, password);
			System.out.println("DB 연결 성공");
		} catch (ClassNotFoundException e) {
			System.out.println("No Driver.");
		} catch (SQLException e) {
			System.out.println("Connection Fail");
		}
	}
}

public class Test2 {

	public static void main(String[] args) throws InterruptedException {
		String[] postposition = { "이", "가", "께서", "에서", "서", "을", "를", "의", "에", "에서", "에게", "로", "이다", "아", "야", "여",
				"시여", "은", "는", "도", "만", "부터", "까지", "마저", "조차", "이나", "거나", "니나", "라니", "라", "라라", "이나이다", "거나이다",
				"니나이다", "라니이다", "라이다", "라라이다", "이나요", "거나요", "니나요", "라니요", "라요", "라라요", "이나냐", "거나냐", "니나냐", "라니냐",
				"라냐", "라라냐", "과", "와", "고", "냐", "니", "라", "라라", "라니", "라니이다", "이나", "거나", "니나", "라니", "라이다", "라라이다",
				"이나요", "거나요", "니나요", "라니요", "라요", "라라요", "이나냐", "거나냐", "니나냐", "라니냐", "라냐", "라라냐", "그리고", "그러니", "그러지만",
				"그러니까", "그러지 않다면", "그러지 않으면", "그러지라도", "그러지 않다고 해도", "그러지 않을지라도", "그러지 않을지라도", "그러지 않을지라도", "그러지 않다면",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도",
				"그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도", "그러지 않다고 해도" };
		// DB에서 문장 가져오기
		String data[] = new String[2000];
		DB_Conn dbc = new DB_Conn();
		try {
			int dataSize = 0;
			int i = 0;
			ResultSet rs;
			String query = "select 증상 from 질환";
			Statement stmt = dbc.con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				if (rs.getString(1) != null) {
					data[i++] = rs.getString(1);
				}
				System.out.println("증상(" + i + ") : " + data[i - 1]);
			}
			stmt.close();
			rs.close();
			dbc.con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> sentences = new ArrayList<String>(Arrays.asList(data));
		sentences.removeAll(Collections.singletonList(null));

		Set<String> words = new HashSet<>();
		for (String sentence : sentences) {
			if(sentence!=null) {
				words.addAll(Arrays.asList(sentence.split(" ")));
				System.out.println(words);
			}
		}

		Map<String, Integer> wordCounts = new HashMap<>();
		for (String word : words) {
			wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
		}

		for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
			if (entry.getValue() > 1) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
			}
		}
	}
}