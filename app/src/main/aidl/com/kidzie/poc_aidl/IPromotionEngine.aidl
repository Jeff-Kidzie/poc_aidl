// IPromotionEngine.aidl
package com.kidzie.poc_aidl;

// Declare any non-default types here with import statements

interface IPromotionEngine {
    /**
         * Sends a raw barcode to the Promotion process and returns the swapped code.
         * Must execute in under 250ms.
         */
        String processBarcode(String rawBarcode);
}