package autotesting.nbankapi.iteration2.senior.models.user;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequestDto extends BaseModel {
    private String username;
    private String password;
}
