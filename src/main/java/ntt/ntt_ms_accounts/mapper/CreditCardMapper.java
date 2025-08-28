package ntt.ntt_ms_accounts.mapper;
import ntt.ntt_ms_accounts.dto.CreateCreditCard;
import ntt.ntt_ms_accounts.dto.CreditCardResponse;
import ntt.ntt_ms_accounts.models.CreditCard;
import ntt.ntt_ms_accounts.models.CardType;
import org.mapstruct.Named;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CreditCardMapper {

    // Request (DTO) -> Model (Entidad)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")                // CardStatus.ACTIVE
    @Mapping(target = "available", source = "creditLimit")          // saldo inicial = límite
    @Mapping(target = "type", source = "type", qualifiedByName = "toCardType")
    CreditCard toModel(CreateCreditCard dto);

    // Model -> Response (DTO)
    @Mapping(target = "type",   source = "type",   qualifiedByName = "enumName")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumName")
    CreditCardResponse toResponse(CreditCard model);

    // ===== Helpers =====

    @Named("toCardType")
    static CardType toCardType(String v) {
        if (v == null) {
            return null;
        }
        return CardType.valueOf(v.trim().toUpperCase());
    }

    @Named("enumName")
    static String enumName(Enum<?> e) {
        return e == null ? null : e.name();
    }

    @AfterMapping
    default void ensureAvailable(@MappingTarget CreditCard card, CreateCreditCard dto) {
        if (card.getAvailable() == null) {
            card.setAvailable(BigDecimal.ZERO);
        }
    }
}