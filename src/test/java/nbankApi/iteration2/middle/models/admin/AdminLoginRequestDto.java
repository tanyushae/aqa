package nbankApi.iteration2.middle.models.admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminLoginRequestDto extends BaseModel {
    private String username;
    private String password;
}
