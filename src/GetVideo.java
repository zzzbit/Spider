import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class GetVideo {
	public static void main(String[] args) {
		try {
			System.setProperty("proxySet", "true");
			System.setProperty("http.proxyHost", "10.108.12.56");
			System.setProperty("http.proxyPort", "8085");

			URL url = new URL(
					"http://f.youku.com/player/getFlvPath/sid/00_00/st/mp4/fileid/03000809005102AD2D787C0365DC4759BC9F5E-6E23-845E-5D54-2D4787D4DB99?K=19b7a482377dbc1e2411485c");
			URLConnection conn = (URLConnection) url.openConnection();
			InputStream is = conn.getInputStream();
			File file = new File(
					"C:\\Users\\zhangzhizhi\\Pictures\\tmp\\tmp.swf");
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
