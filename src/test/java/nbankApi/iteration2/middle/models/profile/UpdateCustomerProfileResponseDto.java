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
public class UpdateCustomerProfileResponseDto extends BaseModel {
 private String message;
 private CustomerResponseDto customer;
}
