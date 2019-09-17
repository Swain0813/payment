package com.payment.trade.dto;


import com.payment.common.dto.enets.JsonCommunicationData;
import com.payment.common.dto.enets.JsonNpxData;
import com.payment.common.dto.enets.TransactionDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@JsonInclude(Include.NON_NULL)
@Data
public class JsonRequest implements  Serializable {

	private static final long serialVersionUID = 4415872387735856251L;

	@JsonProperty("retrieval_ref")
	private String retrievalRef;

	@JsonProperty("mti")
	private String mti;

	@JsonProperty("txn_identifier")
	private String searchId;

	@JsonProperty("process_code")
	private String processCode;

	@JsonProperty("amount")
	private String targetAmount;

	@JsonProperty("stan")
	private String stan;

	@JsonProperty("transaction_time")
	private String transactionTime;

	@JsonProperty("transaction_date")
	private String transactionDate;

	@JsonProperty("entry_mode")
	private String entryMode;

	@JsonProperty("condition_code")
	private String conditionCode;

	@JsonProperty("institution_code")
	private String institutionCode;

	@JsonProperty("card_seq_number")
	private String cardSequenceNumber;

	@JsonProperty("track2_data")
	private String track2Data;

	@JsonProperty("icc_data")
	private String iccData;

	@JsonProperty("pin")
	private String pin;

	@JsonProperty("response_code")
	private String responseCode;

	@JsonProperty("approval_code")
	private String approvalCode;

	@JsonProperty("host_tid")
	private String tid;

	@JsonProperty("host_mid")
	private String mid;

	@JsonProperty("pay_details")
	private String payDetails;

	@JsonProperty("SOF_uri")
	private String sofUri;

	@JsonProperty("user_data")
	private String userData;

	@JsonProperty("sig")
	private String sig;

	@JsonProperty("transmission_time")
	private String transmissionTime;

	@JsonProperty("settlement_date")
	private String settlementDate;

	@JsonProperty("capture_date")
	private String captureDate;

	@JsonProperty("nii")
	private String nii;

	@JsonProperty("acceptor_name")
	private String acceptorName;

	@JsonProperty("loyalty_data")
	private String loyaltyData;

	@JsonProperty("invoice_ref")
	private String invoiceRef;

	@JsonProperty("npx_data")
	private JsonNpxData npxData;

	@JsonProperty("callback_url")
	private String callbackUrl;

	@JsonProperty("cvv")
	private String cvv;

	@JsonProperty("search_count")
	private Integer searchCount;

	@JsonProperty("communication_data")
	private ArrayList<JsonCommunicationData> communicationData;

	@JsonProperty("transactions")
	private ArrayList<TransactionDetail> transactions;

	@JsonProperty("getQRCode")
	private String getQRCode;

	@JsonProperty("qr_code")
	private String qRCode;

	public JsonRequest() {
	}

	public JsonRequest(String retrievalRef, String mti, String searchId, String processCode, String targetAmount, String stan, String transactionTime,
			String transactionDate, String entryMode, String conditionCode, String institutionCode, String cardSequenceNumber, String track2Data,
			String iccData, String pin, String responseCode, String approvalCode, String tid, String mid, String payDetails, String sofUri,
			String userData, String sig, String transmissionTime, String settlementDate, String captureDate, String nii, String acceptorName,
			String loyaltyData, String invoiceRef, JsonNpxData npxData, String callbackUrl, String cvv, Integer searchCount,
			ArrayList<JsonCommunicationData> communicationData, ArrayList<TransactionDetail> transactions, String getQRCode, String qRCode) {
		super();
		this.retrievalRef = retrievalRef;
		this.mti = mti;
		this.searchId = searchId;
		this.processCode = processCode;
		this.targetAmount = targetAmount;
		this.stan = stan;
		this.transactionTime = transactionTime;
		this.transactionDate = transactionDate;
		this.entryMode = entryMode;
		this.conditionCode = conditionCode;
		this.institutionCode = institutionCode;
		this.cardSequenceNumber = cardSequenceNumber;
		this.track2Data = track2Data;
		this.iccData = iccData;
		this.pin = pin;
		this.responseCode = responseCode;
		this.approvalCode = approvalCode;
		this.tid = tid;
		this.mid = mid;
		this.payDetails = payDetails;
		this.sofUri = sofUri;
		this.userData = userData;
		this.sig = sig;
		this.transmissionTime = transmissionTime;
		this.settlementDate = settlementDate;
		this.captureDate = captureDate;
		this.nii = nii;
		this.acceptorName = acceptorName;
		this.loyaltyData = loyaltyData;
		this.invoiceRef = invoiceRef;
		this.npxData = npxData;
		this.callbackUrl = callbackUrl;
		this.cvv = cvv;
		this.searchCount = searchCount;
		this.communicationData = communicationData;
		this.transactions = transactions;
		this.getQRCode = getQRCode;
		this.qRCode = qRCode;
	}



}
