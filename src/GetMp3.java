import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GetMp3 {
	public static void main(String[] args) {
		try {
			System.setProperty("proxySet", "true");
			System.setProperty("http.proxyHost", "10.108.12.56");
			System.setProperty("http.proxyPort", "8085");
			
			URL url = new URL(
					"http://music.baidu.com/data/music/file?link=http://zhangmenshiting.baidu.com/data2/music/34012896/3394889093600128.mp3?xcode=e18da7c79a568b5260279f6048119845");
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();
			File file = new File(
					"C:\\Users\\zhangzhizhi\\Pictures\\tmp\\tmp.mp3");
			FileOutputStream out = new FileOutputStream(file);
			int i = 0;
			while ((i = is.read()) != -1) {
				out.write(i);
			}
			is.close();
			System.out.println("finished!");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
