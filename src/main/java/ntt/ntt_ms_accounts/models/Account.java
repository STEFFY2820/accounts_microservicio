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
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountNumber;

    private String customerId;
    private List<String> holders;
    private List<String> authorizedSigners;

    private AccountType type;
    private AccountStatus status;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maintenanceFee;
    private Integer monthlyMovementLimit;
    private Integer fixedDayAllowed;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;



}
