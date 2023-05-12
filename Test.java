package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

public class Test {

	public static void main(String[] args) throws InterruptedException {
		int i = 1;
		int pageIndex = 0;
		int contTitle = 124;
		int conIndex = 0;
		String index = "";
		String Hurl = "https://www.amc.seoul.kr/asan/healthinfo/disease/diseaseList.do?pageIndex=";
		;
		String asan = "https://www.amc.seoul.kr";
		Pattern p = Pattern.compile("[<>A-Za-z가-힣' '0-9'.()]");
		String[] exceptWords = { "<p>", "<br>", "<strong>", "nbsp", "<span>", "<span stylefontsize 20px>",
				"<span stylefontsize 18px>", "<div classfrview>", "<li>", "<div>", "<dd>", "<br >",
				"<div classinfotext>", "\n", "quot", "middot", "<p classtxt>",
				"<span stylebackgroundcolor rgb(255 255 255)>" };
		String[] contents = { "정의", "원인", "증상", "진단", "치료", "경과", "주의사항" };
		String[] bodyparts = { "골반", "귀", "기타", "눈", "다리", "등/허리", "머리", "목", "발", "배", "생식기", "손", "엉덩이", "유방", "입",
				"전신", "코", "팔", "피부", "가슴", "얼굴" };
		String[] contentsInfo = new String[9];
		try {
			URL url, url2;
			boolean start = true;
			boolean pageStart = true;
			boolean searchStart = false;
			boolean searchDi = false;
			boolean dd = false;
			boolean conInsert = false;
			String sourceLine = "";
			String diseaseLine = "";

			while (start) {
				i++;
				pageStart = true;
				pageIndex = 0;
				System.out.println("부위 index = " + i);
				if (i / 10 < 1) {
					index = "0" + i;
				} else {
					index = "" + i;
				}
				while (pageStart) {
					pageIndex++;
					System.out.println("페이지 index" + pageIndex);
					url = new URL(Hurl + pageIndex + "&partId=B0000" + index);
					BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
					while ((sourceLine = br.readLine()) != null) {
						if (sourceLine.contains("noData")) {
							pageStart = false;
							if (pageIndex == 1) {
								start = false;
							}
							break;
						}
						if (sourceLine.contains("contTitle")) {
							contentsInfo[0] = String.valueOf(contTitle);
							contTitle++;
							searchStart = true;
						}
						if (searchStart) {
							if (sourceLine.contains("href")) {
								System.out.println("\n====================이전 내용====================");
								for (int l = 0; l < contentsInfo.length; l++) {
									if (l == 0) {
										System.out.println("질환번호 : " + contentsInfo[l]);
									} else if (l == 1) {
										System.out.println("질환명 : " + contentsInfo[l]);
									} else {
										System.out.println(contents[l - 2] + " : " + contentsInfo[l]);
									}

								}
								System.out.println("===============================================");
								for (int l = 0; l < contentsInfo.length; l++) {
									contentsInfo[l] = "";
								}
								System.out.println("\n질환번호 : " + contTitle);
								System.out.println("질환명 : " + sourceLine.split("\">")[1].split("</a>")[0]);
								contentsInfo[1] = sourceLine.split("\">")[1].split("</a>")[0];
								url2 = new URL(asan + sourceLine.split("href=\"")[1].split("\"")[0]);
								BufferedReader br2 = new BufferedReader(new InputStreamReader(url2.openStream()));
								while ((diseaseLine = br2.readLine()) != null) {
									String result = "";
									if (diseaseLine.contains("descDl")) {
										searchDi = true;
									}

									if (searchDi) {
										if (diseaseLine.contains("dt")) {
											System.out.println("\n" + diseaseLine.split(">")[1].split("<")[0]);
											for (int k = 0; k < contents.length; k++) {
												if (diseaseLine.split(">")[1].split("<")[0].contains(contents[k])) {
													conIndex = k + 2;
													conInsert = true;
													break;
												} else {
													conInsert = false;
												}
											}
										} else if (diseaseLine.contains("</dl>")) {
											searchDi = false;
										}
										if (diseaseLine.contains("<dd>")) {
											dd = true;
										} else if (diseaseLine.contains("</dd>")) {
											dd = false;
										}
										if (dd && !diseaseLine.contains("<img")) {
											if (conInsert) {
												Matcher m = p.matcher(diseaseLine);
												{
													while (m.find()) {
														contentsInfo[conIndex] += m.group();
													}

												}
												for (int i1 = 0; i1 < exceptWords.length; i1++) {
													if (contentsInfo[conIndex].contains(exceptWords[i1])) {
														contentsInfo[conIndex] = contentsInfo[conIndex]
																.replace(exceptWords[i1], "");
													}
												}
												contentsInfo[conIndex] = contentsInfo[conIndex].replaceAll("\\<.*\\>",
														"");
												System.out.print(contentsInfo[conIndex]);
											}
										}
									}
								}
							} else if (sourceLine.contains("</strong>")) {
								searchStart = false;
							}
							String bodypart = bodyparts[i - 1];
							DB_Conn dbc = new DB_Conn();
							try {
								Thread.sleep(100);
								System.out.println("\n들어간 내용");

								if (contentsInfo[0] != null) {
									PreparedStatement pstmt = dbc.con
											.prepareStatement("insert into 질환 values(?,?,?,?,?,?,?,?,?,?)");
									pstmt.setObject(1, (Object) contentsInfo[0]);
									pstmt.setObject(2, (Object) contentsInfo[1]);
									pstmt.setObject(3, (Object) contentsInfo[2]);
									pstmt.setObject(4, (Object) contentsInfo[3]);
									pstmt.setObject(5, (Object) contentsInfo[4]);
									pstmt.setObject(6, (Object) contentsInfo[5]);
									pstmt.setObject(7, (Object) contentsInfo[6]);
									pstmt.setObject(8, (Object) contentsInfo[7]);
									pstmt.setObject(9, (Object) contentsInfo[8]);
									pstmt.setObject(10, (Object) bodypart);
									pstmt.executeUpdate();
									pstmt.close();
								}
								dbc.con.close();
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					}
					System.out.println("질환 개수 : " + contTitle);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}