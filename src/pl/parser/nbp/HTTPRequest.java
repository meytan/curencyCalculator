package pl.parser.nbp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPRequest {
    public static InputStream sendRequest(String strURL) throws IOException {

        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con.getInputStream();
    }

}
