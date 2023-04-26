import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		int i = 0;
		int pageIndex = 0;
		int contTitle = 0;
		String index = "";
		String Hurl = "https://www.amc.seoul.kr/asan/healthinfo/disease/diseaseList.do?pageIndex=";
		;
		String asan = "https://www.amc.seoul.kr";
		Pattern p = Pattern.compile("[<>A-Za-z가-힣' '0-9'.()]");
		String[] exceptWords = { "<p>", "<br>", "<strong>", "nbsp", "<span>", "<span stylefontsize 20px>",
				"<span stylefontsize 18px>", "<div classfrview>", "<li>", "<div>", "<dd>", "<br >",
				"<div classinfotext>", "\n", "quot", "middot" };
		try {
			URL url, url2;
			boolean start = true;
			boolean pageStart = true;
			boolean searchStart = false;
			boolean searchDi = false;
			boolean dd = false;
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
							contTitle++;
							System.out.println("\n질환번호 : " + contTitle);
							searchStart = true;
						}
						if (searchStart) {
							if (sourceLine.contains("href")) {
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
										} else if (diseaseLine.contains("</dl>")) {
											searchDi = false;
										}
										if (diseaseLine.contains("<dd>")) {
											dd = true;
										} else if (diseaseLine.contains("</dd>")) {
											dd = false;
										}
										if (dd && !diseaseLine.contains("<img")) {
											Matcher m = p.matcher(diseaseLine);
											{
												while (m.find()) {
													result += m.group();
												}
											}
											for (int i1 = 0; i1 < exceptWords.length; i1++) {
												if (result.contains(exceptWords[i1])) {
													result = result.replace(exceptWords[i1], "");
												}
											}
											System.out.print(result);
										}
									}
								}
							} else if (sourceLine.contains("</strong>")) {
								searchStart = false;
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