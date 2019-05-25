package pl.parser.nbp;

import org.xml.sax.SAXException;

class CurrencyCalculatorSAXException extends SAXException {
    private double sellingPrice, buyingPrice;

    CurrencyCalculatorSAXException(double sellingPrice, double buyingPrice) {
        this.sellingPrice = sellingPrice;
        this.buyingPrice = buyingPrice;
    }

    double getSellingPrice() {
        return sellingPrice;
    }

    double getBuyingPrice() {
        return buyingPrice;
    }
}
