package ntt.ntt_ms_accounts.models;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("loans")
public class Loan {

    @Id
    private String id;

    private String customerId;      // persona o empresa (referencia a Customers MS)
    private LoanType type;          // PERSONAL | BUSINESS
    private LoanStatus status;      // ACTIVE | CLOSED | IN_ARREARS | CANCELED

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principal;   // monto otorgado

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal remaining;   // saldo pendiente

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestRateAnnual; // ej. 0.25 para 25%

    private Integer termMonths;     // plazo
    private Instant disbursementDate;
    private Instant nextDueDate;    // próxima cuota

    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
