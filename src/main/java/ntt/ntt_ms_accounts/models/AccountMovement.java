package ntt.ntt_ms_accounts.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
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
