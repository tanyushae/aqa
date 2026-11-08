package autotesting.nbankapi.iteration2.senior.models.admin;

import autotesting.nbankapi.iteration2.senior.enums.UserRole;
import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserResponseDto extends BaseModel {
    private int id;
    private String username;
    private String password;
    private String name;
    private UserRole role;
    private List<CreateAccountResponseDto> accounts;
}
