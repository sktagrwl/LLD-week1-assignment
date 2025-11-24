// InvoiceSRPOCP.java
// Messy starter: Monolith Invoice Service (violates SRP + OCP)

import java.util.*;
import java.io.*;
import java.math.*;

class LineItem {
    String sku;
    int quantity;
    double unitPrice;

    LineItem(String sku, int quantity, double unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}

class Invoice{
    List<LineItem> items;
    Map<String, Double> discounts;
    String email;
    public Invoice(List<LineItem> items, Map<String, Double> discounts, String email){
        this.items = items;
        this.discounts = discounts;
        this.email = email;
    }
}

class SendInvoice{
    public void emailInvoice(String email){
        if (email != null && !email.isEmpty()) {
            System.out.println("[SMTP] Sending invoice to " + email + "...");
        }
    }
}

class InvoiceTest{
    private InvoiceProcess invoiceProcess;
    double computeTotal(Invoice invoice) {
        String rendered = invoiceProcess.processInvoice(invoice);
        int idx = rendered.lastIndexOf("Total:");
        if (idx < 0) throw new RuntimeException("No total");
        String num = rendered.substring(idx + 6).trim();
        return Double.parseDouble(num);
    }
}

class InvoiceProcess{

    private PriceCalculation priceCalculation;
    private DiscountCalculation discountCalculation;
    private TaxCalculation taxCalculation;
    private SendInvoice sendInvoice;
    private RenderInline renderInline;
    private Logging logging;

    public InvoiceProcess(){
        this.priceCalculation = new PriceCalculation();
        this.discountCalculation = new DiscountCalculation();
        this.taxCalculation = new TaxCalculation();
        this.sendInvoice = new SendInvoice();
        this.renderInline = new RenderInline();
        this.logging = new Logging();
    }

    public String processInvoice(Invoice invoice){

        double subtotal = priceCalculation.calculatePrice(invoice);
        double discountTotal = discountCalculation.calculateDiscount(subtotal, invoice);
        double tax = taxCalculation.calculateTax(subtotal, discountTotal);

        double grand = subtotal - discountTotal + tax;

        sendInvoice.emailInvoice(invoice.email);

        logging.processLog(invoice, grand);

        return renderInline.renderPDF(invoice, subtotal, discountTotal, tax, grand);
    }
}

class Logging{
    public void processLog(Invoice invoice, double grand){
        System.out.println("[LOG] Invoice processed for " + invoice.email + " total=" + grand);
    }
}

class RenderInline{
    public String renderPDF(Invoice invoice, double subtotal, double discountTotal, double tax, double grand){
        StringBuilder pdf = new StringBuilder();
        pdf.append("INVOICE\n");
        for (LineItem it : invoice.items) {
            pdf.append(it.sku).append(" x").append(it.quantity).append(" @ ").append(it.unitPrice).append("\n");
        }
        pdf.append("Subtotal: ").append(subtotal).append("\n")
                .append("Discounts: ").append(discountTotal).append("\n")
                .append("Tax: ").append(tax).append("\n")
                .append("Total: ").append(grand).append("\n");
        return pdf.toString();
    }
}

class PriceCalculation{
    public double calculatePrice(Invoice invoice){
        double subtotal = 0.0;
        for (LineItem it : invoice.items){
            subtotal += it.unitPrice * it.quantity;
        }
        return subtotal;
    }
}

class DiscountCalculation{
    public double calculateDiscount(double subtotal, Invoice invoice){
        double discountTotal = 0.0;
        for (Map.Entry<String, Double> e : invoice.discounts.entrySet()) {
            String k = e.getKey();
            double v = e.getValue();
            DiscountStrategy strategy = DiscountRegistry.get(k);
            if(strategy != null){
                discountTotal += strategy.apply(v, subtotal);
            }
        }
        return discountTotal;
    }
}
interface DiscountStrategy{
    double apply(double value, double subtotal);
}

class PercentDiscount implements DiscountStrategy{

    @Override
    public double apply(double value, double subtotal) {
        return subtotal * (value / 100.0);
    }
}

class FlatDiscount implements DiscountStrategy{

    @Override
    public double apply(double value, double subtotal) {
        return value;
    }
}
class DiscountRegistry{

    private static final Map<String, DiscountStrategy> strategies = new HashMap<>();

    static{
        strategies.put("percent_off", new PercentDiscount());
        strategies.put("flat_off", new FlatDiscount());
    }
    public static DiscountStrategy get(String key){
        return strategies.get(key);
    }
}


class TaxCalculation{
    public double calculateTax(double subtotal, double discountTotal){
        double tax = (subtotal - discountTotal) * 0.18;
        return tax;
    }
}

public class InvoiceSRPOCP {
    public static void main(String[] args) {
        InvoiceProcess svc = new InvoiceProcess();

        List<LineItem> items = Arrays.asList(
            new LineItem("BOOK-001", 2, 500.0),
            new LineItem("USB-DRIVE", 1, 799.0)
        );
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("percent_off", 10.0);
        Invoice invoice = new Invoice(items, discounts, "customer@example.com");
        System.out.println(svc.processInvoice(invoice));
    }
}
