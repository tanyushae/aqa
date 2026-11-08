package autotesting.nbankapi.iteration2.senior.requests.skeleton;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.CustomerAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.TransactionDto;
import autotesting.nbankapi.iteration2.senior.models.admin.CreateUserRequestDto;
import autotesting.nbankapi.iteration2.senior.models.admin.CreateUserResponseDto;
import autotesting.nbankapi.iteration2.senior.models.deposit.DepositRequestDto;
import autotesting.nbankapi.iteration2.senior.models.deposit.DepositResponseDto;
import autotesting.nbankapi.iteration2.senior.models.profile.GetCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferRequestDto;
import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferResponseDto;
import autotesting.nbankapi.iteration2.senior.models.user.UserLoginRequestDto;
import autotesting.nbankapi.iteration2.senior.models.user.UserLoginResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

    CREATE_USER_BY_ADMIN(
            "/admin/users",
            CreateUserRequestDto.class,
            CreateUserResponseDto.class
    ),
    LOGIN_USER(
            "/auth/login",
            UserLoginRequestDto.class,
            UserLoginResponseDto.class
    ),
    CREATE_ACCOUNT_BY_USER(
            "/accounts",
            BaseModel.class,
            CreateAccountResponseDto.class
    ),
    DELETE_USER_BY_ADMIN(
            "/admin/users/{userId}",
            BaseModel.class,
            BaseModel.class
    ),
    GET_CUSTOMER_ACCOUNT(
            "/customer/accounts",
            BaseModel.class,
            CustomerAccountResponseDto.class
    ),
    DEPOSIT(
            "/accounts/deposit",
            DepositRequestDto.class,
            DepositResponseDto.class
    ),
    GET_TRANSACTION(
            "/accounts/{accountId}/transactions",
            BaseModel.class,
            TransactionDto.class
    ),
    ACCOUNT_TRANSFER(
            "/accounts/transfer",
            AccountTransferRequestDto.class,
            AccountTransferResponseDto.class
    ),
    GET_CUSTOMER_PROFILE(
            "/customer/profile",
            BaseModel.class,
            GetCustomerProfileResponseDto.class
    ),
    UPDATE_CUSTOMER_PROFILE(
            "/customer/profile",
            UpdateCustomerProfileRequestDto.class,
            UpdateCustomerProfileResponseDto.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
