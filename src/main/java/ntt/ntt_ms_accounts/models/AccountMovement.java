package ntt.ntt_ms_accounts.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "account_movements")

public class AccountMovement {
    @Id
    private String id;

    @Indexed
    private String accountId;

    @Indexed
    private Instant date;

    private MovementType type;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private String reference;

    @CreatedDate
    private Instant createdAt;
}
