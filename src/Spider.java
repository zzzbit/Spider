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
	private String strHomePage = null; // ��ҳ��ַ
	private String strHost = null; // ������ַ
	private ArrayList<String> WaitingList = new ArrayList<String>(); // �洢δ����URL
	private ArrayList<String> AllUrlList = new ArrayList<String>(); // ����ɵ�URL
	private ArrayList<String> AllPicList = new ArrayList<String>(); // ����ͼƬ��URL
	private String charset = "UTF-8";
	private boolean charsetFlag = false;
	private int index = 0; // �ļ�����������
	private int count = 0; // �Ѽ����ҳ��Url����
	private String regDomain = ".*//.*/"; // ��������ʽ
	private String regUrl = "([\"\']http(s)?://|href=).+?[\"\']"; // URL��ַƥ������ʽ
	// ����Ĳ�����Ҫ���õ�
	private int intThreadNum = 10; // �߳���
	private String fileDirectory = "MyFile\\"; // �����ļ���·��
	private boolean AgentFlag = false; // �����־
	private String IpAddress = "10.108.12.56"; // ����IP
	private String Port = "8085"; // ����˿�
	private boolean TimeFlag = false; // �������ʱ���־
	private long MaxTime = 30000; // �����ʱ�䣬��λ���룬��Ϊ1����
	private boolean MaxUrlFlag = true; // ���ҳ������־
	private int MaxUrl = 20; // ���Url��
	// ��������ʽ
	// private String regTitle = "<h1>.*</h1>"; //��������ʽ
	// private String regPicUrl = "height=\"350\" src=.+?\""; //ͼƬ��ַ����ʽ
	// private String regNextPageUrl = "href=.*��һҳ"; //��һҳ����ҳ��ַ
	// private String regContentUrl = "href.*SEO"; //������ҳ��ַ����ʽ
	// �Ա�����ʽ
	// private String regTitle = "<title>.+?<"; //��������ʽ
	// private String regPicUrl = "J_ImgBooth.+?data-hasZoom"; //ͼƬ��ַ����ʽ
	// private String regNextPageUrl = "href=.+?class=\"page-next"; //��һҳ����ҳ��ַ
	// private String regContentUrl = "class=\"summary\".+?target"; //������ҳ��ַ����ʽ
	// ��������ʽ����������
	// private String regTitle = "<title>.+?title"; //��������ʽ
	// private String regPicUrl = "img.+?jpg\""; //ͼƬ��ַ����ʽ
	// private String regNextPageUrl = "href=.+?class=\"nextpage"; //��һҳ����ҳ��ַ
	// private String regContentUrl = "name=\"Pic\">\n.+?title"; //������ҳ��ַ����ʽ
	// ����ѷ����ʽ
	// private String regTitle = "name=\"description\".+?>"; //��������ʽ
	// private String regPicUrl = "main-image-inner-wrapper\">\n.+?jpg\"";
	// //ͼƬ��ַ����ʽ
	// private String regNextPageUrl = "pagnNext\">.+?>"; //��һҳ����ҳ��ַ
	// private String regContentUrl = "productImage\">.+?>"; //������ҳ��ַ����ʽ
	// ��������ʽ
	// private String regTitle = "name=\"description\".+?>"; //��������ʽ
	// private String regPicUrl = "midimg.+?jpg\""; //ͼƬ��ַ����ʽ
	// private String regNextPageUrl = "s_top_nextpage.+?>"; //��һҳ����ҳ��ַ
	// private String regContentUrl = "<div class=\"newview\"></div>\n.+?class";
	// //������ҳ��ַ����ʽ
	// �ѹ�mp3
	private String regTitle = "<title>.+?title"; // ��������ʽ
	private String regPicUrl = "href.+?.mp3\""; // ͼƬ��ַ����ʽ
	private String regNextPageUrl = "window.open.+?'.+?'"; // ��һҳ����ҳ��ַ
	private String regContentUrl = "window.open.+?'.+?'"; // ������ҳ��ַ����ʽ

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

	// ����Ϣд��txt�ļ�
	private synchronized boolean add2File(String s) {
		try {
			// ���������ļ�Ŀ¼
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
			System.out.println("������Ϣ�ļ�ʧ��!");
			return false;
		}
	}

	// ��Դ��д���ļ������û�д����ʽ��
	public synchronized boolean addHtmlFile(String urlString) {
		try {
			URL url = new URL(urlString);
			URLConnection conn;
			BufferedReader bReader;
			String rLine;
			StringBuffer stringBuffer = new StringBuffer();

			// �õ���վ����
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
			// �õ�Դ��
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

			// �����ļ���
			if (!new File(fileDirectory).isDirectory()) {
				new File(fileDirectory).mkdir();
			}

			// д���ļ�
			OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
					fileDirectory + "@" + "content" + ".html", true), charset);
			w.write(stringBuffer.toString());
			w.flush();
			w.close();
			return true;
		} catch (Exception e) {
			System.out.println("������Ϣ�ļ�ʧ��!");
			return false;
		}
	}

	// �ӵȴ�������ȡ��һ��
	private synchronized String getWaitingUrl() {
		String tmpAUrl = WaitingList.get(0);
		WaitingList.remove(0);
		return tmpAUrl;
	}

	// ��ƥ�䵽���ַ�����õ�URL��ַ��������Ҫ��ȫ
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

	public void startSpider() { // ���û��ṩ������վ�㿪ʼ������������ҳ�����ץȡ
		// ���ô���
		if (AgentFlag) {
			System.setProperty("proxySet", "true");
			System.setProperty("http.proxyHost", IpAddress);
			System.setProperty("http.proxyPort", Port);
		}

		// ��¼��ʼʱ��
		long begin = System.currentTimeMillis();

		// ����List��
		WaitingList.add(strHomePage);
		AllUrlList.add(strHomePage);

		// ��������
		Pattern p = Pattern.compile(regDomain, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(strHomePage);
		if (m.find()) {
			strHost = m.group(0);
		} else {
			strHost = strHomePage + "/";
		}

		// ����URL����Ӧ����ҳ����ץȡ
		String tmp = getWaitingUrl();
		this.getWebByUrl(tmp);

		// ���̵߳��ô������
		for (int i = 0; i < intThreadNum; i++) {
			new Thread(new Processer()).start();
		}

		// �ж����߳���ֹ����
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

	// �Ժ�����������url����ץȡ
	public void getWebByUrl(String strUrl) {
		try {
			// ��URL�ж�������ҳ
			URL url = new URL(strUrl);
			URLConnection conn;
			BufferedReader bReader;
			String rLine;
			StringBuffer stringBuffer = new StringBuffer();

			// �õ���վ����
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
			// �õ�Դ��
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

	// �����µ���ҳ����ȡ���к��е�������Ϣ������true��ʾ����ȡURLҳ�棬�����ʾ������ҳ��
	public boolean getUrlByString(String inputArgs) {
		boolean pageFlag = false;
		Pattern p;
		Matcher m;

		// �õ�����Ƭ��ҳ��URL
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
		// �ٴ�����һҳ�����
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

	public boolean getContentByString(String strUrl, String inputArgs) { // �����µ���ҳ����ȡ���к��е�������Ϣ
		String msgString;
		String imageString = "null";
		String titleString = "null";
		Pattern p;
		Matcher m;

		// �õ�ͼƬURL
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

		// �õ�����
		p = Pattern.compile(regTitle, Pattern.CASE_INSENSITIVE);
		m = p.matcher(inputArgs);
		if (m.find()) {
			titleString = m.group(0).substring(4, m.group(0).length() - 2);
		} else {
			return false;
		}

		// ��Ϣ�������
		msgString = strUrl + "\n" + imageString + "\n" + titleString;
		if (add2File(msgString)) {
			return true;
		}
		return false;
	}

	class Processer implements Runnable { // ������ץȡ�߳�
		public void run() {
			while (!WaitingList.isEmpty()) {
				getWebByUrl(getWaitingUrl());
			}
		}
	}
}
