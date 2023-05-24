package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		String[] postposition = { "이", "가", "께서", "에서", "서", "을", "를", "의", "에", "아", "야", "여", "시여", "은", "는", "도",
				"만", "마저", "조차", "이나", "과", "다.", "과", "암", "닥", "락" };
		Pattern regex = Pattern.compile("[가-힣 .]");
		String data[] = new String[2000];
		// DB에서 문장 가져오기
		DB_Conn dbc = new DB_Conn();
		List<String> sentences = new ArrayList<String>();
		Set<String> words = new HashSet<>();
		Map<String, Integer> wordCounts = new HashMap<>();
		try {
			int i = 0;
			ResultSet rs;
			String query = "select 증상 from 질환";
			Statement stmt = dbc.con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (rs.getString(1) != null) {
					data[i++] = rs.getString(1);
				}
			}
			stmt.close();
			rs.close();
			dbc.con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String str : data) {
			StringBuffer sb = new StringBuffer();
			if (str != null) {
				Matcher regexMatcher = regex.matcher(str);
				while (regexMatcher.find()) {
					sb.append(regexMatcher.group());
				}

				String result = sb.toString();
				sentences.add(result);
			}
		}
		for (String sen : sentences) {
			words.addAll(Arrays.asList(sen.split(" ")));
		}
		for (String word : words) {
			for (String pp : postposition) {
				if (word.contains(pp)) {
					word = word.replaceAll(pp + "$", "");
				}
				if (word.length() > 1) {
					wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
				}
			}
		}
		List<Map.Entry<String, Integer>> entryList = new LinkedList<>(wordCounts.entrySet());
		entryList.sort(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});
		String[] popularWords = new String[300];
		int cnt = 0;
		for (Map.Entry<String, Integer> entry : entryList) {
			if (cnt < 300 && entry.getKey().contains("기침")) {
				popularWords[cnt] = cnt + 1 + ".키워드 : (" + entry.getKey() + ") 중복 횟수 : " + entry.getValue();
				cnt++;
			}
		}
		for (String keyword : popularWords) {
			if (keyword != null) {
				System.out.println(keyword);
			}
		}
	}
}
