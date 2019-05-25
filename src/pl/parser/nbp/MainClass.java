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


    public static void main(String[] args) {

        long programStart = System.currentTimeMillis();

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
        catch (IndexOutOfBoundsException e) {
            System.out.println("Wrong arguments! Try again!");
        }
        catch (SAXException e){
            System.out.println(e.getMessage());
        }
        catch (DateTimeException e) {
            System.out.println("Invalid date or date format!");
            System.out.println("Remember valid date format is yyyy-mm-dd!");
        }
        catch (IOException e){
            System.out.println("There is something wrong with internet connection.");
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        System.out.println("\n\n\n" + (System.currentTimeMillis() - programStart));
    }

    // #1 ~190 000
}
