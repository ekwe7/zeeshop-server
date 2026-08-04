package com.ekwe_hub.zeeshopserver.sales.service.interfaces;

import java.util.UUID;

public interface ReceiptService {

    /**
     * Generates a PDF receipt for the given sale ID as a byte array.
     *
     * @param saleId UUID of the sale
     * @return byte array containing the PDF binary content
     */
    byte[] generateReceiptPdf(UUID saleId);
}
