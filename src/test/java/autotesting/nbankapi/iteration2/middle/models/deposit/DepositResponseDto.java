package autotesting.nbankapi.iteration2.middle.models.deposit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import autotesting.nbankapi.iteration2.middle.models.BaseModel;
import autotesting.nbankapi.iteration2.middle.models.account.TransactionDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponseDto extends BaseModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<TransactionDto> transactions;
}