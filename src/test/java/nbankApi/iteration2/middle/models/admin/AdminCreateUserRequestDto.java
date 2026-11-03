package nbankApi.iteration2.middle.models.admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;
import nbankApi.iteration2.middle.enums.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminCreateUserRequestDto extends BaseModel {
    private String username;
    private String password;
    private UserRole role;
}
