import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spider {
	private String strHomePage = null; // 主页地址
	private String strHost = null; // 主机地址
	private ArrayList<String> WaitingList = new ArrayList<String>(); // 存储未处理URL
	private ArrayList<String> AllUrlList = new ArrayList<String>(); // 已完成的URL
	private ArrayList<String> AllPicList = new ArrayList<String>(); // 所有图片的URL
	private String charset = "UTF-8";
	private boolean charsetFlag = false;
	private int index = 0; // 文件按递增命名
	private int count = 0; // 已加入的页面Url总数
	private String regDomain = ".*//.*/"; // 域名正则式
	private String regUrl = "([\"\']http(s)?://|href=).+?[\"\']"; // URL网址匹配正则式
	// 下面的参数是要设置的
	private int intThreadNum = 10; // 线程数
	private String fileDirectory = "MyFile\\"; // 保存文件夹路径
	private boolean AgentFlag = false; // 代理标志
	private String IpAddress = "10.108.12.56"; // 代理IP
	private String Port = "8085"; // 代理端口
	private boolean TimeFlag = false; // 限制最大时间标志
	private long MaxTime = 30000; // 限制最长时间，单位毫秒，此为1分钟
	private boolean MaxUrlFlag = true; // 最大页面数标志
	private int MaxUrl = 20; // 最大Url数
	// 京东正则式
	// private String regTitle = "<h1>.*</h1>"; //标题正则式
	// private String regPicUrl = "height=\"350\" src=.+?\""; //图片网址正则式
	// private String regNextPageUrl = "href=.*下一页"; //下一页的网页网址
	// private String regContentUrl = "href.*SEO"; //内容网页网址正则式
	// 淘宝正则式
	// private String regTitle = "<title>.+?<"; //标题正则式
	// private String regPicUrl = "J_ImgBooth.+?data-hasZoom"; //图片网址正则式
	// private String regNextPageUrl = "href=.+?class=\"page-next"; //下一页的网页网址
	// private String regContentUrl = "class=\"summary\".+?target"; //内容网页网址正则式
	// 当当正则式，编码问题
	// private String regTitle = "<title>.+?title"; //标题正则式
	// private String regPicUrl = "img.+?jpg\""; //图片网址正则式
	// private String regNextPageUrl = "href=.+?class=\"nextpage"; //下一页的网页网址
	// private String regContentUrl = "name=\"Pic\">\n.+?title"; //内容网页网址正则式
	// 亚马逊正则式
	// private String regTitle = "name=\"description\".+?>"; //标题正则式
	// private String regPicUrl = "main-image-inner-wrapper\">\n.+?jpg\"";
	// //图片网址正则式
	// private String regNextPageUrl = "pagnNext\">.+?>"; //下一页的网页网址
	// private String regContentUrl = "productImage\">.+?>"; //内容网页网址正则式
	// 凡客正则式
	// private String regTitle = "name=\"description\".+?>"; //标题正则式
	// private String regPicUrl = "midimg.+?jpg\""; //图片网址正则式
	// private String regNextPageUrl = "s_top_nextpage.+?>"; //下一页的网页网址
	// private String regContentUrl = "<div class=\"newview\"></div>\n.+?class";
	// //内容网页网址正则式
	// 搜狗mp3
	private String regTitle = "<title>.+?title"; // 标题正则式
	private String regPicUrl = "href.+?.mp3\""; // 图片网址正则式
	private String regNextPageUrl = "window.open.+?'.+?'"; // 下一页的网页网址
	private String regContentUrl = "window.open.+?'.+?'"; // 内容网页网址正则式

	public Spider(String s) {
		this.strHomePage = s;
	}

	public static void main(String[] args) {
		// String arg0 =
		// "http://search.360buy.com/Search?keyword=%E9%9E%8B&enc=utf-8&area=1";
		// String arg0 =
		// "http://s.taobao.com/search?spm=a230r.1.7.1.47PWMn&q=%D0%AC&commend=all&ssid=s5-e&search_type=item&sourceId=tb.index&initiative_id=tbindexz_20130321&bcoffset=1&s=0#J_relative";
		// String arg0 = "http://searchb.dangdang.com/?key=%D0%AC";
		// String arg0 =
		// "http://www.amazon.cn/s/ref=nb_sb_noss?__mk_zh_CN=%E4%BA%9A%E9%A9%AC%E9%80%8A%E7%BD%91%E7%AB%99&url=search-alias%3Daps&field-keywords=%E9%9E%8B";
		// String arg0 = "http://s.vancl.com/search?k=%E9%9E%8B&orig=3";
		String arg0 = "http://bbs.locoy.com/spider-66379-1-1.html";
		Spider gw = new Spider(arg0);
		gw.addHtmlFile(arg0);
		// gw.startSpider();
	}

	// 将信息写入txt文件
	private synchronized boolean add2File(String s) {
		try {
			// 创建接收文件目录
			if (!new File(fileDirectory).isDirectory()) {
				new File(fileDirectory).mkdir();
			}
			BufferedWriter w = new BufferedWriter(new FileWriter(fileDirectory
					+ (index++) + ".txt"));
			w.write(s);
			w.flush();
			w.close();
			return true;
		} catch (Exception e) {
			System.out.println("加入信息文件失败!");
			return false;
		}
	}

	// 将源码写入文件，供用户写正则式用
	public synchronized boolean addHtmlFile(String urlString) {
		try {
			URL url = new URL(urlString);
			URLConnection conn;
			BufferedReader bReader;
			String rLine;
			StringBuffer stringBuffer = new StringBuffer();

			// 得到网站编码
			if (!charsetFlag) {
				conn = url.openConnection();
				conn.setDoOutput(true);
				bReader = new BufferedReader(new InputStreamReader(
						url.openStream()));
				while ((rLine = bReader.readLine()) != null) {
					Matcher m = Pattern.compile("(charset)|(encoding).+?\".+?\"").matcher(
							rLine);
					if (m.find()) {
						charset = m.group(0).substring(
								m.group(0).indexOf('\"') + 1,
								m.group(0).length() - 1);
						System.out.println(charset);
						charsetFlag = true;
						break;
					}
				}
			}
			charsetFlag = true;
			// 得到源码
			conn = url.openConnection();
			conn.setDoOutput(true);
			// String str_cookie=null;
			// conn.setRequestProperty("Cookie", str_cookie);
			bReader = new BufferedReader(new InputStreamReader(
					url.openStream(), charset));
			while ((rLine = bReader.readLine()) != null) {
				stringBuffer.append(rLine + "\n");
			}

			if (bReader != null) {
				bReader.close();
			}

			// 创建文件夹
			if (!new File(fileDirectory).isDirectory()) {
				new File(fileDirectory).mkdir();
			}

			// 写入文件
			OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
					fileDirectory + "@" + "content" + ".html", true), charset);
			w.write(stringBuffer.toString());
			w.flush();
			w.close();
			return true;
		} catch (Exception e) {
			System.out.println("加入信息文件失败!");
			return false;
		}
	}

	// 从等待队列里取出一个
	private synchronized String getWaitingUrl() {
		String tmpAUrl = WaitingList.get(0);
		WaitingList.remove(0);
		return tmpAUrl;
	}

	// 从匹配到的字符串里得到URL地址，如有需要则补全
	private String checkUrl(String string) {
		Pattern p = Pattern.compile(regUrl, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(string);
		if (m.find()) {
			String tmpString = m.group(0);
			if (tmpString.indexOf('\"') != -1) {
				tmpString = tmpString.substring(tmpString.indexOf('\"') + 1,
						tmpString.lastIndexOf('\"'));
			} else {
				tmpString = tmpString.substring(tmpString.indexOf('\'') + 1,
						tmpString.lastIndexOf('\''));
			}
			if (tmpString.contains("http")) {
				System.out.println(tmpString);
				return tmpString;
			} else {
				tmpString = strHost + tmpString;
				System.out.println(tmpString);
				return tmpString;
			}
		} else {
			System.out.println("Error:" + string);
			return null;
		}
	}

	public void startSpider() { // 由用户提供的域名站点开始，对所有链接页面进行抓取
		// 设置代理
		if (AgentFlag) {
			System.setProperty("proxySet", "true");
			System.setProperty("http.proxyHost", IpAddress);
			System.setProperty("http.proxyPort", Port);
		}

		// 记录开始时间
		long begin = System.currentTimeMillis();

		// 加入List中
		WaitingList.add(strHomePage);
		AllUrlList.add(strHomePage);

		// 查找域名
		Pattern p = Pattern.compile(regDomain, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(strHomePage);
		if (m.find()) {
			strHost = m.group(0);
		} else {
			strHost = strHomePage + "/";
		}

		// 对新URL所对应的网页进行抓取
		String tmp = getWaitingUrl();
		this.getWebByUrl(tmp);

		// 多线程调用处理程序
		for (int i = 0; i < intThreadNum; i++) {
			new Thread(new Processer()).start();
		}

		// 判断主线程终止条件
		while (true) {
			if (WaitingList.isEmpty() && Thread.activeCount() == 1) {
				System.out.println("Finished!");
				break;
			}
			if (TimeFlag && System.currentTimeMillis() - begin > MaxTime) {
				WaitingList.clear();
				TimeFlag = false;
			}
		}
	}

	// 对后续解析出的url进行抓取
	public void getWebByUrl(String strUrl) {
		try {
			// 从URL中读整个网页
			URL url = new URL(strUrl);
			URLConnection conn;
			BufferedReader bReader;
			String rLine;
			StringBuffer stringBuffer = new StringBuffer();

			// 得到网站编码
			if (!charsetFlag) {
				conn = url.openConnection();
				conn.setDoOutput(true);
				bReader = new BufferedReader(new InputStreamReader(
						url.openStream()));
				while ((rLine = bReader.readLine()) != null) {
					Matcher m = Pattern.compile("(charset)|(encoding).+?\".+?\"").matcher(
							rLine);
					if (m.find()) {
						charset = m.group(0).substring(
								m.group(0).indexOf('\"') + 1,
								m.group(0).length() - 1);
						System.out.println(charset);
						charsetFlag = true;
						break;
					}
				}
			}
			charsetFlag = true;
			// 得到源码
			conn = url.openConnection();
			conn.setDoOutput(true);
			bReader = new BufferedReader(new InputStreamReader(
					url.openStream(), charset));
			while ((rLine = bReader.readLine()) != null) {
				stringBuffer.append(rLine + "\n");
			}

			if (bReader != null) {
				bReader.close();
			}

			if (!getUrlByString(stringBuffer.toString())) {
				getContentByString(strUrl, stringBuffer.toString());
			}
			stringBuffer = null;
		} catch (Exception e) {
			System.out.println("getWebByUrl error");
		}
	}

	// 解析新的网页，提取其中含有的链接信息，返回true表示是提取URL页面，否则表示是内容页面
	public boolean getUrlByString(String inputArgs) {
		boolean pageFlag = false;
		Pattern p;
		Matcher m;

		// 得到内容片网页的URL
		String tmpStr = inputArgs;
		p = Pattern.compile(regContentUrl, Pattern.CASE_INSENSITIVE);
		m = p.matcher(tmpStr);
		boolean blnp = m.find();
		while (blnp == true) {
			String url = checkUrl(m.group(0));
			if (MaxUrlFlag && count >= MaxUrl) {
				return true;
			}
			if (!AllUrlList.contains(url)) {
				WaitingList.add(url);
				AllUrlList.add(url);
				count++;
			}
			tmpStr = tmpStr.substring(m.end(), tmpStr.length());
			m = p.matcher(tmpStr);
			blnp = m.find();
			pageFlag = true;
		}

		if (!pageFlag) {
			return false;
		}
		// 再处理下一页的情况
		p = Pattern.compile(regNextPageUrl, Pattern.CASE_INSENSITIVE);
		m = p.matcher(inputArgs);
		if (m.find()) {
			String url = checkUrl(m.group(0));
			WaitingList.add(url);
			AllUrlList.add(url);
			pageFlag = true;
		}
		return pageFlag;
	}

	public boolean getContentByString(String strUrl, String inputArgs) { // 解析新的网页，提取其中含有的链接信息
		String msgString;
		String imageString = "null";
		String titleString = "null";
		Pattern p;
		Matcher m;

		// 得到图片URL
		p = Pattern.compile(regPicUrl, Pattern.CASE_INSENSITIVE);
		m = p.matcher(inputArgs);
		if (m.find()) {
			imageString = checkUrl(m.group(0));
			if (AllPicList.contains(imageString)) {
				return false;
			}
			AllPicList.add(imageString);
			new Thread(new GetPicture(imageString, fileDirectory + index
					+ imageString.substring(imageString.lastIndexOf('.'))))
					.start();
		} else {
			return false;
		}

		// 得到标题
		p = Pattern.compile(regTitle, Pattern.CASE_INSENSITIVE);
		m = p.matcher(inputArgs);
		if (m.find()) {
			titleString = m.group(0).substring(4, m.group(0).length() - 2);
		} else {
			return false;
		}

		// 信息汇总输出
		msgString = strUrl + "\n" + imageString + "\n" + titleString;
		if (add2File(msgString)) {
			return true;
		}
		return false;
	}

	class Processer implements Runnable { // 独立的抓取线程
		public void run() {
			while (!WaitingList.isEmpty()) {
				getWebByUrl(getWaitingUrl());
			}
		}
	}
}
