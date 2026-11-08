package autotesting.nbankapi.iteration2.senior.models.account;

import autotesting.nbankapi.iteration2.senior.enums.TransactionType;
import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto extends BaseModel {
    private int id;
    private double amount;
    private TransactionType type;
    private String timestamp;
    private int relatedAccountId;
}
