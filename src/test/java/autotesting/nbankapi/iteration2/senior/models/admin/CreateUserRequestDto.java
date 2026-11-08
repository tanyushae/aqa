package autotesting.nbankapi.iteration2.senior.models.admin;

import autotesting.nbankapi.iteration2.senior.generators.GeneratingRule;
import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequestDto extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9]{8,15}$")
    private String username;
    @GeneratingRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingRule(regex = "^USER$")
    private String role;
}
