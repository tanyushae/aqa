package nbankApi.iteration2.middle.support.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;

/**
 * Созданный пользователь (его токен) и два его счёта
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithTwoAccounts {
    private String token;
    private CustomerAccountResponseDto firstAccount;
    private CustomerAccountResponseDto secondAccount;
}
