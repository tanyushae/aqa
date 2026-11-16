package autotesting.nbankapi.iteration2.middle.support.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import autotesting.nbankapi.iteration2.middle.models.account.CustomerAccountResponseDto;

/**
 * Созданный пользователь (его токен) и его единственный счёт
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithAccount {
    private String token;
    private CustomerAccountResponseDto account;
}
