package autotesting.nbankapi.iteration2.senior.models.transfer;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTransferRequestDto extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;
}
