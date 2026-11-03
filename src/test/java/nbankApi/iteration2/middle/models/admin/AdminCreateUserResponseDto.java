package nbankApi.iteration2.middle.models.admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminCreateUserResponseDto extends BaseModel {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<CustomerAccountResponseDto> accounts;
}
