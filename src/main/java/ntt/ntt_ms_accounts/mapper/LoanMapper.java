package ntt.ntt_ms_accounts.mapper;

import ntt.ntt_ms_accounts.dto.CreateLoanRequest;
import ntt.ntt_ms_accounts.dto.LoanResponse;
import ntt.ntt_ms_accounts.models.Loan;
import ntt.ntt_ms_accounts.models.LoanType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)              // lo setea el service
    @Mapping(target = "remaining", ignore = true)           // se inicializa = principal
    @Mapping(target = "disbursementDate", ignore = true)    // lo setea el service
    @Mapping(target = "nextDueDate", ignore = true)         // lo setea el service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "type", source = "type", qualifiedByName = "toLoanType")
    Loan toModel(CreateLoanRequest dto);

    @Mapping(target = "type", source = "type", qualifiedByName = "enumName")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumName")
    LoanResponse toResponse(Loan model);

    // Helpers
    @Named("toLoanType")
    static LoanType toLoanType(String v) {
        return v == null ? null : LoanType.valueOf(v.trim().toUpperCase());
    }

    @Named("enumName")
    static String enumName(Enum<?> e) {
        return e == null ? null : e.name();
    }
}
