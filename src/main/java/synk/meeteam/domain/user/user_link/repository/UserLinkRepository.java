package synk.meeteam.domain.user.user_link.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synk.meeteam.domain.user.user_link.entity.UserLink;

public interface UserLinkRepository extends JpaRepository<UserLink, Long> {

    void deleteAllByCreatedBy(Long id);
}