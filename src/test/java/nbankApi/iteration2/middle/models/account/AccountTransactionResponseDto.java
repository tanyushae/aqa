package nbankApi.iteration2.middle.models.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTransactionResponseDto extends BaseModel {
    private List<TransactionDto> transactions;
}
