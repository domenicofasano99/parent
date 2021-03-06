package com.bok.parent;

import com.bok.parent.be.helper.AccountHelper;
import com.bok.parent.be.service.AccountService;
import com.bok.parent.be.service.SecurityService;
import com.bok.parent.integration.dto.AccountRegistrationDTO;
import com.bok.parent.model.Account;
import com.bok.parent.model.Credentials;
import com.bok.parent.repository.AccountRepository;
import com.bok.parent.repository.AuditLogRepository;
import com.bok.parent.repository.TemporaryAccountRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Component
public class ModelTestUtil {

    public static final Faker faker = new Faker();

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountHelper accountHelper;

    @Autowired
    AccountService accountService;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    TemporaryAccountRepository temporaryAccountRepository;

    @Autowired
    SecurityService securityService;

    @Autowired
    PasswordEncoder passwordEncoder;

    private AccountRegistrationDTO createRegistrationDTO(String email, String password) {
        AccountRegistrationDTO registrationDTO = new AccountRegistrationDTO();
        registrationDTO.name = faker.name().name().replace(".", "");
        registrationDTO.surname = faker.name().lastName().replace(".", "");
        registrationDTO.birthdate = faker.date().birthday();
        registrationDTO.fiscalCode = "FFFFFF99F99F999F";
        registrationDTO.gender = faker.demographic().sex();
        registrationDTO.business = false;

        registrationDTO.credentials = new AccountRegistrationDTO.CredentialsDTO(email);

        AccountRegistrationDTO.AddressDTO address = new AccountRegistrationDTO.AddressDTO();
        address.city = faker.address().city();
        address.country = faker.address().country();
        address.postalCode = faker.address().zipCode();
        address.county = "county";
        address.street = faker.address().streetAddress();
        address.houseNumber = faker.address().buildingNumber();
        registrationDTO.address = address;

        AccountRegistrationDTO.MobileDTO mobile = new AccountRegistrationDTO.MobileDTO();
        mobile.number = faker.phoneNumber().cellPhone();
        mobile.icc = faker.phoneNumber().extension();
        registrationDTO.mobile = mobile;

        return registrationDTO;
    }

    public Credentials createAccountWithCredentials() {
        String email = faker.internet().emailAddress();
        String password = sha256Hex(faker.internet().password());
        Account a = new Account();
        a.setCredentials(new Credentials(email, passwordEncoder.encode(password)));
        accountRepository.save(a);
        return new Credentials(email, password);
    }

    public Account createAccount() {
        Account a = new Account();
        a.setCredentials(new Credentials(faker.internet().emailAddress(), passwordEncoder.encode(sha256Hex(faker.internet().password()))));
        return accountRepository.save(a);
    }

    public void clearAll() {
        auditLogRepository.deleteAll();
        temporaryAccountRepository.deleteAll();
        accountRepository.deleteAll();
    }
}
