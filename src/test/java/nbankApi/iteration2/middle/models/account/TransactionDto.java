package nbankApi.iteration2.middle.models.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.enums.TransactionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    private int id;
    private double amount;
    private TransactionType type;
    private String timestamp;
    private int relatedAccountId;
}