package pl.parser.nbp;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class MainClass {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate start = LocalDate.parse("2013-01-28",dtf);
//        LocalDate end = LocalDate.parse("2014-01-31",dtf);
//        List<String> filenames =getFilenamesBetweenDates(start, end);
//        for(String filename : filenames){
//            System.out.println(filename);
//        }

        Document doc = getXML("c001z130102.xml");

        System.out.println(doc.getDocumentElement().getNodeName());


    }

    public static InputStream sendRequest(String strURL) throws IOException {

        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con.getInputStream();


    }

    public static List<String> getFilenamesBetweenDates(LocalDate start, LocalDate end) throws IOException {
        List<String> filenameList = new ArrayList<>();
        String url;
        int currentYear = LocalDate.now().getYear();
        int startMonth = start.getMonthValue();
        int startDay = start.getDayOfMonth();
        int endMonth = end.getMonthValue();
        int endDay = end.getDayOfMonth();
        for(int i = start.getYear();i<=end.getYear();i++){

            if(i!=currentYear)
                url = "http://www.nbp.pl/kursy/xml/dir" + i + ".txt";
            else
                url = "http://www.nbp.pl/kursy/xml/dir.txt";

            BufferedReader response = new BufferedReader(new InputStreamReader(sendRequest(url)));
            String line;
            while ((line = response.readLine()) != null) {
                if(line.startsWith("c")){
                    if(i==start.getYear()) {
                        int month = Integer.parseInt(line.substring(7, 9));
                        int day = Integer.parseInt(line.substring(9, 11));

                        if (month < startMonth)
                            continue;
                        else if (month == startMonth)
                            if (day < startDay)
                                continue;
                    }
                    else if(i == end.getYear()){
                        int month = Integer.parseInt(line.substring(7, 9));
                        int day = Integer.parseInt(line.substring(9, 11));
                        if(month > endMonth)
                            break;
                        else if(month == endMonth)
                            if(day>endDay)
                                break;
                    }


                    filenameList.add(line + ".xml");
                }
            }
            response.close();
        }

        return filenameList;
    }

    public static Document getXML(String xmlName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(sendRequest("http://www.nbp.pl/kursy/xml/" + xmlName));
        return doc;
    }

}
