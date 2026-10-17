package nbankApi.iteration2.middle.support.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerWithAccount {
    private String token;
    private CustomerAccountResponseDto account;
}
