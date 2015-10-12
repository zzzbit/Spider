import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MyDownload {
	private ArrayList<String> WaitingList = new ArrayList<String>(); // 存储未处理URL
	private ArrayList<String> AllUrlList = new ArrayList<String>(); // 已完成的URL
	static int successcount = 0;
	static int failcount = 0;
	private int threadNum = 10;
	private String filePath = "url.txt";
	private String saveDir = "download\\";
	private int index = 0;

	public MyDownload(int threadNum, String filePath, String saveDir) {
		this.threadNum = threadNum;
		this.filePath = filePath;
		this.saveDir = saveDir;
	}

	public void startDownload() {
		try {
			String s = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath)));
			while ((s = br.readLine()) != null) {
				WaitingList.add(s);
				AllUrlList.add(s);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("打开文件失败");
		}
		// 多线程调用处理程序
		for (int i = 0; i < threadNum; i++) {
			new Thread(new Processer()).start();
		}

		// 判断主线程终止条件
		while (true) {
			if (WaitingList.isEmpty() && Thread.activeCount() == 1) {
				System.out.println("Finished!");
				break;
			}
		}
	}

	// 从等待队列里取出一个
	private synchronized String getWaitingUrl() {
		String tmpAUrl = WaitingList.get(0);
		WaitingList.remove(0);
		return tmpAUrl;
	}

	private void GetPic(String urlString) {
		try {
			if (AllUrlList.contains(urlString)){
				return;
			}
			String fileName = saveDir + (index++)
					+ urlString.substring(urlString.lastIndexOf('.'));
			URL url = new URL(urlString);
			URLConnection conn = (URLConnection) url.openConnection();
			InputStream is = conn.getInputStream();
			File file = new File(fileName);
			FileOutputStream out = new FileOutputStream(file);
			int i = 0;
			while ((i = is.read()) != -1) {
				out.write(i);
			}
			is.close();
			out.close();
			successcount++;
		} catch (Exception e) {
			failcount++;
		}
	}

	class Processer implements Runnable { // 独立的抓取线程
		public void run() {
			while (!WaitingList.isEmpty()) {
				GetPic(getWaitingUrl());
			}
		}
	}

	// args[0]是线程数量，args[1]是url文件位置，args[2]是保存位置
	public static void main(String[] args) {
		int threadNum = 10;
		String filePath = "url.txt";
		String saveDir = "download\\";
		new File(saveDir).mkdirs();
		if (args.length == 1) {
			threadNum = Integer.parseInt(args[0]);
		} else if (args.length == 2) {
			threadNum = Integer.parseInt(args[0]);
			filePath = args[1];
		} else if (args.length == 3) {
			threadNum = Integer.parseInt(args[0]);
			filePath = args[1];
			saveDir = args[2];
		}
		MyDownload myDownload = new MyDownload(threadNum, filePath, saveDir);
		myDownload.startDownload();
	}
}