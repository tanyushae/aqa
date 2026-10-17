package nbankApi.iteration2.middle.models.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbankApi.iteration2.middle.models.BaseModel;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerProfileRequestDto extends BaseModel {
    private String name;
}
