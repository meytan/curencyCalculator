package pl.parser.nbp;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class NAPCurrencyCalculator {

    private String searchedCurrency;
    private List<Double> sellPrices;
    private List<Double> buyPrices;
    private List<String> filenames;
    private List<InputStream> xmls;


    NAPCurrencyCalculator(String searchedCurrency) {
        this.searchedCurrency = searchedCurrency;
        sellPrices = new ArrayList<>();
        buyPrices = new ArrayList<>();
        filenames = new ArrayList<>();
        xmls = new ArrayList<>();
    }

    private void getXMLs(){

        final int threadsNumber = 50;

        List<List<String>> list = new ArrayList<>();

        for( int i = 0; i < filenames.size(); i+= filenames.size()/threadsNumber){
            int start = i;
            if(filenames.size() % threadsNumber > list.size())
                i++;
            int end = i + filenames.size()/threadsNumber;
            list.add(filenames.subList(start,end));
        }

        ExecutorService es = Executors.newCachedThreadPool();
        for(List<String> sublist : list){
                es.execute(() -> {
                    for(int i = 0; i<sublist.size(); i++){
                        URL url;
                        try {
                            url = new URL("http://www.nbp.pl/kursy/xml/" + sublist.get(i));
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("GET");
                            addXML(con.getInputStream());
                        } catch (IOException e) {
                            if(e.getMessage().startsWith("Server returned HTTP response code: 429")) {
                                i--;
                            }
                            else{
                                System.out.println("There is something wrong with internet connection");
                            }
                        }
                    }
                });
        }
        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("OR HERE");
        }
    }


    void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = parserFactory.newSAXParser();
        SAXHandler handler = new SAXHandler(searchedCurrency);

        getXMLs();

        for (InputStream xmlFile : xmls)
        {
            try {
                saxParser.parse(xmlFile,handler);
            } catch (CurrencyCalculatorSAXException e) {
                buyPrices.add(e.getBuyingPrice());
                sellPrices.add(e.getSellingPrice());
            }

        }
    }

    private double mean(List<Double> list){
        OptionalDouble average = list.stream().mapToDouble(a-> a).average();
        return average.isPresent() ? average.getAsDouble() : 0;
    }


    double getStandardDeviationOfSellingPrices(){
        double mean = mean(sellPrices);
        double tmp = 0;
        for (Double i : sellPrices)
        {
            tmp += Math.pow(i - mean, 2);
        }


        double meanOfDiffs = tmp / sellPrices.size();
        return Math.sqrt(meanOfDiffs);
    }

    double getMeanOfBuyingPrices(){
        return mean(buyPrices);
    }

    List<String> getFilenamesBetweenDates(LocalDate start, LocalDate end) throws IOException {

        if(start.isAfter(end))
        {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

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
                    if(i == end.getYear()){
                        int month = Integer.parseInt(line.substring(7, 9));
                        int day = Integer.parseInt(line.substring(9, 11));
                        if(month > endMonth)
                            break;
                        else if(month == endMonth)
                            if(day>endDay)
                                break;
                    }


                    filenames.add(line + ".xml");
                }
            }
            response.close();
        }
        return filenames;
    }

    private synchronized void addXML(InputStream is){
        xmls.add(is);
    }

    private static InputStream sendRequest(String strURL) throws IOException {

        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con.getInputStream();
    }


}
