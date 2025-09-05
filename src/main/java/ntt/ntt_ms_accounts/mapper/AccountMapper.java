package ntt.ntt_ms_accounts.mapper;

import ntt.ntt_ms_accounts.dto.OpenAccountRequest;
import ntt.ntt_ms_accounts.dto.MovementResponse;
import ntt.ntt_ms_accounts.dto.AccountResponse;
import ntt.ntt_ms_accounts.dto.BalanceResponse;
import ntt.ntt_ms_accounts.models.Account;
import ntt.ntt_ms_accounts.models.AccountMovement;
import ntt.ntt_ms_accounts.models.AccountType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;


import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    // Request DTO -> Modelo
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "type", qualifiedByName = "toAccountType")
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "maintenanceFee", ignore = true)
    @Mapping(target = "monthlyMovementLimit", ignore = true)
    @Mapping(target = "fixedDayAllowed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toModel(OpenAccountRequest dto);

    // Modelo -> Response DTO
    @Mapping(target = "type",   source = "type",   qualifiedByName = "enumName")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumName")
    @Mapping(target = "balance", source = "balance", qualifiedByName = "bigToStr")
    @Mapping(target = "holders", source = "holders")
    @Mapping(target = "authorizedSigners", source = "authorizedSigners")
    @Mapping(target = "maintenanceFee")
    @Mapping(target = "monthlyMovementLimit")
    @Mapping(target = "fixedDayAllowed")
    AccountResponse toResponse(Account model);

    // Modelo -> MovementResponse
    @Mapping(target = "type", source = "type", qualifiedByName = "enumName")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "bigToStr")
    MovementResponse toResponse(AccountMovement mv);

    // Modelo -> BalanceResponse
    @Mapping(target = "accountId", source = "id")
    @Mapping(target = "balance", source = "balance", qualifiedByName = "bigToStr")
    BalanceResponse toBalance(Account a);

    // Helpers

    @Named("enumName")
    public static String enumName(Enum<?> e) {
        return e == null ? null : e.name();
    }

    @Named("bigToStr")
    public static String bigToStr(BigDecimal b) {
        return b == null ? "0" : b.toPlainString();
    }

    @Named("toAccountType")
    public static AccountType toAccountType(String value) {
        if(value == null) {return null;}
        return AccountType.valueOf(value.trim().toUpperCase());
    }

    @AfterMapping
    default void defaultBalanceIfNull(@MappingTarget Account a, OpenAccountRequest dto) {
        if (a.getBalance() == null)
        {a.setBalance(BigDecimal.ZERO);}
    }
}