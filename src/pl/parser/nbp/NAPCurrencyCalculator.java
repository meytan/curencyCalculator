package pl.parser.nbp;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static pl.parser.nbp.HTTPRequest.sendRequest;

class NAPCurrencyCalculator {

    private String searchedCurrency;
    private List<Double> sellPrices;
    private List<Double> buyPrices;
    private List<String> filenames;

    NAPCurrencyCalculator(String searchedCurrency) {
        this.searchedCurrency = searchedCurrency;
        sellPrices = new ArrayList<>();
        buyPrices = new ArrayList<>();
        filenames = new ArrayList<>();
    }

    void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = parserFactory.newSAXParser();
        SAXHandler handler = new SAXHandler(searchedCurrency);
        long timer = 0;
        for (String filename : filenames)
        {
            long start = System.currentTimeMillis();
            InputStream xmlFile = sendRequest("http://www.nbp.pl/kursy/xml/"+filename);
            timer += start - System.currentTimeMillis();
            try {
                saxParser.parse(xmlFile,handler);
            } catch (CurrencyCalculatorSAXException e) {
                buyPrices.add(e.getBuyingPrice());
                sellPrices.add(e.getSellingPrice());
            }

        }

        System.out.println(timer);
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



}
