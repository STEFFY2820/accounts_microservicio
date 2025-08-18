package ntt.ntt_ms_accounts.models;


import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
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
