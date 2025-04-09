package io.project.receiver.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author armen
 */
@Entity
@Table(name = "payment_events_received", schema = "public")
@Data
public class PaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Basic(optional = false)
    @Column(name = "client_Id", updatable = false)
    private String clientId;

    @Basic(optional = false)
    @Column(name = "client_account_number", updatable = false)
    private String clientAccountNumber;

    @Basic(optional = false)
    @Column(name = "notification_id", updatable = false, unique = true)
    private String notificationId;

    @Basic(optional = false)
    @Column(name = "timestamp", updatable = false)
    private Long timestamp;

    @Basic(optional = false)
    @Column(name = "currency", updatable = false)
    private String currency;

    @Column(name = "received_amount", updatable = false)
    private BigDecimal receivedAmount = BigDecimal.ZERO;

    @Basic(optional = false)
    @Column(name = "beneficiary", updatable = false)
    private String beneficiary;

    @Basic(optional = false)
    @Column(name = "transaction_id", updatable = false, unique = true)
    private String transactionId;

    @Basic(optional = false)
    @Column(name = "details", updatable = false)
    private String details;

    @Basic(optional = false)
    @Column(name = "status", updatable = false)
    private String status;

    @Basic(optional = false)
    @Column(name = "transaction_code", updatable = false)
    private String transactionCode;

    @Column(name = "received")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime received;

}
