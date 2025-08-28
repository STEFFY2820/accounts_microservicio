package ntt.ntt_ms_accounts.models;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("credit_cards")
public class CreditCard {
    @Id
    private String id;

    @Indexed(unique = true)
    private String cardNumber;

    private String customerId;    // persona o empresa
    private CardType type;        // PERSONAL | BUSINESS
    private CardStatus status;    // ACTIVE | BLOCKED | CANCELED

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal creditLimit;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal available;

    private Integer closingDay;
    private Integer dueDay;

    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
