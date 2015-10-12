import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GetPicture implements Runnable{
	static int count = 0;
	String urlString;
	String filePath;
	GetPicture(String urlString,String filePath){
		this.urlString = urlString;
		this.filePath = filePath;
	}
	private void GetPic() {
		try {
			URL url = new URL(urlString);
			URLConnection conn = (URLConnection) url.openConnection();
			InputStream is = conn.getInputStream();
			File file = new File(filePath);
			FileOutputStream out = new FileOutputStream(file);
			int i = 0;
			while ((i = is.read()) != -1) {
				out.write(i);
			}
			is.close();
			out.close();
		} catch (Exception e) {
			System.out.println("GetPicture failed!");
		}
	}
	public void run() {
		GetPic();
	}
}