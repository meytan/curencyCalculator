package pl.parser.nbp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SAXHandler extends DefaultHandler {

    private boolean foundCurrency = false;
    private boolean isCurrencyName = false, isSellingPrice = false, isBuyingPrice = false;

    private String searchedCurrency;
    private double sellingPrice, buyingPrice;

    SAXHandler(String searchedCurrency) {
        this.searchedCurrency = searchedCurrency;
    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void characters(char[] ch, int start, int length) throws  CurrencyCalculatorSAXException {
        if(isCurrencyName) {
            if(searchedCurrency.equals(new String(ch,start,length)))
                foundCurrency = true;
            else
                isCurrencyName = false;
        }
        else if(foundCurrency){
            String replace = new String(ch, start, length).replace(',', '.');
            if(isSellingPrice) {
                sellingPrice = Double.parseDouble(replace);
                isSellingPrice = false;
                foundCurrency = false;
                throw new CurrencyCalculatorSAXException(sellingPrice, buyingPrice);
            }
            else if(isBuyingPrice){
                buyingPrice = Double.parseDouble(replace);
                isBuyingPrice = false;
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if(qName.equals("kod_waluty"))
            isCurrencyName = true;
        else if(foundCurrency){
            if(qName.equals("kurs_kupna"))
                isBuyingPrice = true;
            else if(qName.equals("kurs_sprzedazy"))
                isSellingPrice = true;

        }
    }

    @Override
    public void endDocument() throws SAXException {

    }
}
