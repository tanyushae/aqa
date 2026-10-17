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
public class AccountTransferResponseDto extends BaseModel {
    private int senderAccountId;
    private String message;
    private double amount;
    private int receiverAccountId;
}