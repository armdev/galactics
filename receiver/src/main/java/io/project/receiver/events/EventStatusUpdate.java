package io.project.receiver.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author armena
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventStatusUpdate implements Serializable {

    @Serial
    private static final long serialVersionUID = -8958157555709456242L;

    @JsonProperty("id")
    private String id;    
    @JsonProperty("transaction_id")
    private String transactionId;  
    @JsonProperty("status")
    private String status;
  

}
