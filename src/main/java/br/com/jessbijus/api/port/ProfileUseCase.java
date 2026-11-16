package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.AdminProfile;
import br.com.jessbijus.api.domain.ProfileInput;

public interface ProfileUseCase {
    AdminProfile get(String subject);
    AdminProfile save(String subject, ProfileInput input);
}
