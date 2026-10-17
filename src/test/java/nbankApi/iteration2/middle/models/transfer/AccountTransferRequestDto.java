package nbankApi.iteration2.middle.models.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTransferRequestDto extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;
}
