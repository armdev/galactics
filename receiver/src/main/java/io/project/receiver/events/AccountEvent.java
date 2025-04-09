package io.project.receiver.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author armena
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -8958157555709456242L;

    @JsonProperty("id")
    private String id;
    @JsonProperty("client_Id")
    private String clientId;
    @JsonProperty("client_account_number")
    private String clientAccountNumber;
    @JsonProperty("notificationId")
    private String notificationId;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("received_amount")
    private BigDecimal receivedAmount = BigDecimal.ZERO;
    @JsonProperty("beneficiary")
    private String beneficiary;
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("details")
    private String details;
    @JsonProperty("status")
    private String status;
    @JsonProperty("transaction_code")
    private String transactionCode;

}
