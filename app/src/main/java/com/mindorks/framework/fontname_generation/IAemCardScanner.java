package com.mindorks.framework.fontname_generation;


public interface IAemCardScanner {

void onScanMSR(String buffer, CardReader.CARD_TRACK cardtrack);

void onScanDLCard(String buffer);

void onScanRCCard(String buffer);

void onScanRFD(String buffer);

void onScanPacket(String buffer);


}
