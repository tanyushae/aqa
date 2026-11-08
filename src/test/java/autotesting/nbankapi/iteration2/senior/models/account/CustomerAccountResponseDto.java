package autotesting.nbankapi.iteration2.senior.models.account;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerAccountResponseDto extends BaseModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<TransactionDto> transactions;
}