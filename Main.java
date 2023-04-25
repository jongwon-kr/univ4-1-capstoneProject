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
		String index = "";
		String Hurl = "https://www.amc.seoul.kr/asan/healthinfo/disease/diseaseList.do?pageIndex=";
		;
		Pattern p = Pattern.compile("[<>A-Za-z가-힣' '0-9'.]");
		String[] exceptWords = { "<p>", "<br>", "<strong>", "nbsp", "<span>", "<span stylefontsize 20px>",
				"<span stylefontsize 18px>", "<div classfrview>", "<li>", "<div>" };
		try {
			URL url;
			StringBuffer sourceCode = new StringBuffer();
			boolean start = true;
			boolean pageStart = true;
			String sourceLine = "";
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
							if (pageIndex == 1)
								start = false;
							break;
						}
					}
				}
			}
		} catch (Exception e) {

		}
	}
}
