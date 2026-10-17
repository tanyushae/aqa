package nbankApi.iteration2.middle.models.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;
import nbankApi.iteration2.middle.enums.UserRole;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponseDto extends BaseModel {
    private int id;
    private String username;
    private String password;
    private String name;
    private UserRole role;
    private List<CustomerAccountResponseDto> accounts;
}