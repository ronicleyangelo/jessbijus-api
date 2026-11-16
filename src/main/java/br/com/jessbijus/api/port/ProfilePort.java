package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.AdminProfile;
import java.util.Optional;

public interface ProfilePort {
    Optional<AdminProfile> findBySubject(String subject);
    AdminProfile save(String subject, AdminProfile profile);
}
