package com.ekwe_hub.zeeshopserver.sales.service.impl;

import com.ekwe_hub.zeeshopserver.sales.entity.Sale;
import com.ekwe_hub.zeeshopserver.sales.entity.SaleItem;
import com.ekwe_hub.zeeshopserver.sales.repository.interfaces.SaleRepository;
import com.ekwe_hub.zeeshopserver.sales.service.interfaces.ReceiptService;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptServiceImpl implements ReceiptService {

    private final SaleRepository saleRepository;

    @Value("${app.shop.name:ZeeShop Store}")
    private String shopName;

    @Value("${app.shop.address:123 Commerce Way, Business District}")
    private String shopAddress;

    @Value("${app.shop.phone:+234 800 123 4567}")
    private String shopPhone;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] generateReceiptPdf(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", saleId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Styling fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);

            // Header - Shop Details
            Paragraph shopNamePara = new Paragraph(shopName, titleFont);
            shopNamePara.setAlignment(Element.ALIGN_CENTER);
            document.add(shopNamePara);

            Paragraph shopDetailsPara = new Paragraph(shopAddress + "\nTel: " + shopPhone, subTitleFont);
            shopDetailsPara.setAlignment(Element.ALIGN_CENTER);
            shopDetailsPara.setSpacingAfter(15);
            document.add(shopDetailsPara);

            // Line separator
            Paragraph receiptHeading = new Paragraph("SALES RECEIPT", sectionTitleFont);
            receiptHeading.setAlignment(Element.ALIGN_CENTER);
            receiptHeading.setSpacingAfter(15);
            document.add(receiptHeading);

            // Metadata Table (Ref Number, Date, Status, Payment Type, Customer Details)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{50f, 50f});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("Receipt Ref: " + (sale.getReferenceNumber() != null ? sale.getReferenceNumber() : "N/A"), normalFont));
            leftCell.addElement(new Paragraph("Payment Type: " + (sale.getPaymentType() != null ? sale.getPaymentType().name() : "CASH"), normalFont));
            leftCell.addElement(new Paragraph("Status: " + sale.getStatus().name(), normalFont));

            if (sale.getCustomerName() != null || sale.getCustomerPhone() != null || sale.getCustomerEmail() != null) {
                StringBuilder custInfo = new StringBuilder("Customer: ");
                if (sale.getCustomerName() != null) custInfo.append(sale.getCustomerName());
                if (sale.getCustomerPhone() != null) custInfo.append(" (").append(sale.getCustomerPhone()).append(")");
                if (sale.getCustomerEmail() != null) custInfo.append(" - ").append(sale.getCustomerEmail());
                leftCell.addElement(new Paragraph(custInfo.toString(), normalFont));
            }

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            String formattedDate = sale.getCreatedAt() != null ? sale.getCreatedAt().format(DATE_FORMATTER) : "N/A";
            Paragraph datePara = new Paragraph("Date: " + formattedDate, normalFont);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(datePara);

            if (sale.getDueDate() != null) {
                Paragraph dueDatePara = new Paragraph("Due Date: " + sale.getDueDate(), normalFont);
                dueDatePara.setAlignment(Element.ALIGN_RIGHT);
                rightCell.addElement(dueDatePara);
            }

            metaTable.addCell(leftCell);
            metaTable.addCell(rightCell);
            metaTable.setSpacingAfter(15);
            document.add(metaTable);

            // Items Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{45f, 15f, 20f, 20f});

            addHeaderCell(table, "Item Description", tableHeaderFont);
            addHeaderCell(table, "Qty", tableHeaderFont);
            addHeaderCell(table, "Unit Price", tableHeaderFont);
            addHeaderCell(table, "Total Amount", tableHeaderFont);

            for (SaleItem item : sale.getItems()) {
                table.addCell(new PdfPCell(new Phrase(item.getProduct() != null ? item.getProduct().getName() : "Unknown Product", normalFont)));

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase(formatAmount(item.getUnitPrice()), normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);

                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                PdfPCell itemTotalCell = new PdfPCell(new Phrase(formatAmount(itemTotal), normalFont));
                itemTotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(itemTotalCell);
            }

            table.setSpacingAfter(15);
            document.add(table);

            // Total Breakdown Section (Subtotal, Discount, Tax, Total)
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{70f, 30f});

            addSummaryRow(totalTable, "Subtotal:", formatAmount(sale.getSubtotalAmount()), normalFont);
            if (sale.getDiscountAmount() != null && sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(totalTable, "Discount:", "-" + formatAmount(sale.getDiscountAmount()), normalFont);
            }
            addSummaryRow(totalTable, "Total Amount:", formatAmount(sale.getTotalAmount()), totalFont);

            totalTable.setSpacingAfter(25);
            document.add(totalTable);

            // Footer Message
            Paragraph footerPara = new Paragraph("Thank you for shopping with us!", subTitleFont);
            footerPara.setAlignment(Element.ALIGN_CENTER);
            document.add(footerPara);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while generating PDF receipt", e);
        }

        return baos.toByteArray();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setBackgroundColor(Color.DARK_GRAY);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(6);
        table.addCell(header);
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }
}
