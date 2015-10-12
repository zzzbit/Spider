import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

public class GetWeb {
	private int webDepth = 2; // 爬虫深度
	private int intThreadNum = 10; // 线程数
	private String strHomePage = ""; // 主页地址
	private String myDomain; // 域名
	private String fPath = "web"; // 储存网页文件的目录名
	private ArrayList<String> arrUrls = new ArrayList<String>(); // 存储未处理URL
	private ArrayList<String> arrUrl = new ArrayList<String>(); // 存储所有URL供建立索引
	private Hashtable<String, Integer> allUrls = new Hashtable<String, Integer>(); // 存储所有URL的网页号
	private Hashtable<String, Integer> deepUrls = new Hashtable<String, Integer>(); // 存储所有URL深度
	private int intWebIndex = 0; // 网页对应文件下标，从0开始
	private String charset = "GB2312";
	private String report = "";
	private long startTime;
	private int webSuccessed = 0;
	private int webFailed = 0;

	public GetWeb(String s) {
		this.strHomePage = s;
	}

	public GetWeb(String s, int i) {
		this.strHomePage = s;
		this.webDepth = i;
	}

	public synchronized void addWebSuccessed() {
		webSuccessed++;
	}

	public synchronized void addWebFailed() {
		webFailed++;
	}

	public synchronized void addReport(String s) {
		try {
			report += s;
			PrintWriter pwReport = new PrintWriter(new FileOutputStream(
					"report.txt"));
			pwReport.println(report);
			pwReport.close();
		} catch (Exception e) {
			System.out.println("生成报告文件失败!");
		}
	}

	public synchronized String getAUrl() {
		String tmpAUrl = arrUrls.get(0);
		arrUrls.remove(0);
		return tmpAUrl;
	}

	public synchronized String getUrl() {
		String tmpUrl = arrUrl.get(0);
		arrUrl.remove(0);
		return tmpUrl;
	}

	public synchronized Integer getIntWebIndex() {
		intWebIndex++;
		return intWebIndex;
	}

	public static void main(String[] args) {
		String arg0 = "http://www.360buy.com/product/1003026730.html";
		int arg1 = 2;
		GetWeb gw = new GetWeb(arg0, arg1);
		gw.getWebByHomePage();
	}

	public void getWebByHomePage() { // 由用户提供的域名站点开始，对所有链接页面进行抓取
		startTime = System.currentTimeMillis();
		this.myDomain = getDomain();
		if (myDomain == null) {
			System.out.println("Wrong input!");
			return;
		}

		System.out.println("Homepage = " + strHomePage);
		addReport("Homepage = " + strHomePage + "!\n");
		System.out.println("Domain = " + myDomain);
		addReport("Domain = " + myDomain + "!\n");
		arrUrls.add(strHomePage);
		arrUrl.add(strHomePage);
		allUrls.put(strHomePage, 0);
		deepUrls.put(strHomePage, 1);

		File fDir = new File(fPath);
		if (!fDir.exists()) {
			fDir.mkdir();
		}

		System.out.println("Start!");
		this.addReport("Start!\n");
		String tmp = getAUrl(); // 取出新的URL
		this.getWebByUrl(tmp, charset, allUrls.get(tmp) + ""); // 对新URL所对应的网页进行抓取
		int i = 0;
		for (i = 0; i < intThreadNum; i++) {
			new Thread(new Processer(this)).start();
		}
		while (true) {
			if (arrUrls.isEmpty() && Thread.activeCount() == 1) {
				long finishTime = System.currentTimeMillis();
				long costTime = finishTime - startTime;
				System.out.println("\nFinished!");
				addReport("\nFinished!\n");
				System.out.println("Cost time = " +

						costTime + "ms");
				addReport("Cost time = " + costTime + "ms"

						+ "\n");
				System.out.println("Total url number = "
						+ (webSuccessed + webFailed) + " Successed: "
						+ webSuccessed + " Failed: " +

						webFailed);
				addReport("Total url number = " + (webSuccessed + webFailed)
						+ " Successed: " + webSuccessed + " Failed: "
						+ webFailed

						+ "\n");

				String strIndex = "";
				String tmpUrl = "";
				while (!arrUrl.isEmpty()) {
					tmpUrl = getUrl();
					strIndex += "Web depth:" + deepUrls.get(tmpUrl)
							+ " Filepath: " + fPath + "/web"
							+ allUrls.get(tmpUrl) + ".htm" + "url:" + tmpUrl
							+ "\n\n";
				}
				System.out.println(strIndex);
				try {
					PrintWriter pwIndex = new PrintWriter(new FileOutputStream(
							"fileindex.txt"));
					pwIndex.println(strIndex);
					pwIndex.close();
				} catch (Exception e) {
					System.out.println("生成索引文件失败!");
				}
				break;
			}
		}
	}

	public void getWebByUrl(String strUrl, String charset, String fileIndex) { // 对后续解析出的url进行抓取
		try {
			// if(charset==null||"".equals(charset))charset="utf-8";
			System.out.println("Getting web by url: " + strUrl);
			addReport("Getting web by url: " + strUrl + "\n");

			URL url = new URL(strUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			InputStream is = null;
			is = url.openStream();

			String filePath = fPath + "/web" + fileIndex + ".htm";
			PrintWriter pw = null;
			FileOutputStream fos = new FileOutputStream(filePath);
			OutputStreamWriter writer = new OutputStreamWriter(fos);
			pw = new PrintWriter(writer);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					is));
			StringBuffer sb = new StringBuffer();
			String rLine = null;
			String tmp_rLine = null;
			while ((rLine = bReader.readLine()) != null) {
				tmp_rLine = rLine;
				int str_len = tmp_rLine.length();
				if (str_len > 0) {
					sb.append("\n" + tmp_rLine);
					pw.println(tmp_rLine);
					pw.flush();
					if (deepUrls.get(strUrl) < webDepth){
						getImgUrlByString(tmp_rLine, strUrl);
						getContentByString(tmp_rLine, strUrl);
					}
				}
				tmp_rLine = null;
			}
			is.close();
			pw.close();
			System.out.println("Get web successfully! " + strUrl);
			addReport("Get web successfully! " + strUrl + "\n");
			addWebSuccessed();
		} catch (Exception e) {
			System.out.println("Get web failed! " + strUrl);
			addReport("Get web failed! " + strUrl + "\n");
			addWebFailed();
		}
	}

	public String getDomain() { // 判断用户所提供URL是否为域名地址
		String reg = "(?<=http\\://[a-zA-Z0-9]{0,100}[.]{0,1})[^.\\s]*?\\.(com|cn|net|org|biz|info|cc|tv)";
		Pattern p = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(strHomePage);
		boolean blnp = m.find();
		if (blnp == true) {
			return m.group(0);
		}
		return null;
	}

	public void getUrlByString(String inputArgs, String strUrl) { // 解析新的网页，提取其中含有的链接信息
		String tmpStr = inputArgs;
		String regUrl = "(?<=(href=)[\"]?[\']?)[http://][^\\s\"\'\\?]*("
				+ myDomain + ")[^\\s\"\'>]*";
		Pattern p = Pattern.compile(regUrl, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(tmpStr);
		boolean blnp = m.find();
		// int i = 0;
		while (blnp == true) {
			if (!allUrls.containsKey(m.group(0))) {
				System.out.println("Find a new url,depth:"
						+ (deepUrls.get(strUrl) + 1) + " " + m.group(0));
				addReport("Find a new url,depth:" + (deepUrls.get(strUrl) + 1)
						+ " " + m.group(0) + "\n");
				arrUrls.add(m.group(0));
				arrUrl.add(m.group(0));
				allUrls.put(m.group(0), getIntWebIndex());
				deepUrls.put(m.group(0), (deepUrls.get(strUrl) + 1));
			}
			tmpStr = tmpStr.substring(m.end(), tmpStr.length());
			m = p.matcher(tmpStr);
			blnp = m.find();
		}
	}
	//得到图片URL
	public void getImgUrlByString(String inputArgs, String strUrl) { // 解析新的网页，提取其中含有的链接信息
		String tmpStr = inputArgs;
		String regUrl = "height=\"350\" src=.+?.jpg";
		Pattern p = Pattern.compile(regUrl, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(tmpStr);
		boolean blnp = m.find();
		// int i = 0;
		while (blnp == true) {
			if (!allUrls.containsKey(m.group(0))) {
				String imageString = m.group().substring(m.group().lastIndexOf('\"')+1);
				System.out.println(imageString);
//				GetPicture.GetPic(imageString);
				addReport("Find a new url,depth:" + (deepUrls.get(strUrl) + 1)
						+ " " + m.group(0) + "\n");
			}
			tmpStr = tmpStr.substring(m.end(), tmpStr.length());
			m = p.matcher(tmpStr);
			blnp = m.find();
		}
	}
	public void getContentByString(String inputArgs, String strUrl) { // 解析新的网页，提取其中含有的链接信息
		String tmpStr = inputArgs;
		String regUrl = "<title>.*</title>";
		Pattern p = Pattern.compile(regUrl, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(tmpStr);
		boolean blnp = m.find();
		while (blnp == true) {
			if (!allUrls.containsKey(m.group(0))) {
				String contentString = m.group().substring(7, m.group().length()-8);
				System.out.println(contentString);
				addReport("Find a new url,depth:" + (deepUrls.get(strUrl) + 1)
						+ " " + m.group(0) + "\n");
				break;
			}
			tmpStr = tmpStr.substring(m.end(), tmpStr.length());
			m = p.matcher(tmpStr);
			blnp = m.find();
		}
		
		regUrl = "店铺：.*<";
		p = Pattern.compile(regUrl, Pattern.CASE_INSENSITIVE);
		m = p.matcher(tmpStr);
		blnp = m.find();
		while (blnp == true) {
			if (!allUrls.containsKey(m.group(0))) {
				String contentString = m.group().substring(3, m.group().length()-1);
				System.out.println(contentString);
				addReport("Find a new url,depth:" + (deepUrls.get(strUrl) + 1)
						+ " " + m.group(0) + "\n");
				break;
			}
			tmpStr = tmpStr.substring(m.end(), tmpStr.length());
			m = p.matcher(tmpStr);
			blnp = m.find();
		}
		
		
	}
	class Processer implements Runnable { // 独立的抓取线程
		GetWeb gw;

		public Processer(GetWeb g) {
			this.gw = g;
		}

		public void run() {
			// Thread.sleep(5000);
			while (!arrUrls.isEmpty()) {
				String tmp = getAUrl();
				getWebByUrl(tmp, charset, allUrls.get(tmp) + "");
			}
		}
	}
}
