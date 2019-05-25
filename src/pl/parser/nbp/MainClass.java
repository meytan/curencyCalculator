package pl.parser.nbp;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class MainClass {


    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        try {
            String currencyName = args[0];
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(args[1], dtf);
            LocalDate end = LocalDate.parse(args[2], dtf);

            NAPCurrencyCalculator currencyCalculator = new NAPCurrencyCalculator(currencyName);
            currencyCalculator.getFilenamesBetweenDates(start, end);
            currencyCalculator.parse();

            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.HALF_UP);
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.GERMAN));

            System.out.println(df.format(currencyCalculator.getMeanOfBuyingPrices()));
            System.out.println(df.format(currencyCalculator.getStandardDeviationOfSellingPrices()));


        }
        catch(IndexOutOfBoundsException e){
            System.out.println("Wrong arguments! Try again!");
        }
        catch (DateTimeException e){
            System.out.println("Invalid date or date format!");
            System.out.println("Remember valid date format is yyyy-mm-dd!");
        }





        // #1 40727
        // #2 37681

        //# parsing xml 2 years:
        //DOM - 18505


//        4.153322908366532
//        0.0475848739242345
//        91989
//        Getting filenames: 805
//        Sending requests: 72535
//        Building DOM: 18505
//        Parsing xml: 92

//        #SAX
//        4.153322908366532
//        0.0475848739242345
//        60062
//        Getting filenames: 762
//        Sending requests: 58448
//        Building DOM: 790

    }






}
