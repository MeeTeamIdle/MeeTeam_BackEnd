package synk.meeteam.domain.user.user_link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import synk.meeteam.domain.user.user_link.entity.UserLink;
import synk.meeteam.domain.user.user_link.entity.UserLinkMapper;
import synk.meeteam.domain.user.user_link.repository.UserLinkRepository;
import synk.meeteam.domain.user.user_link.service.UserLinkService;

@ExtendWith(MockitoExtension.class)
public class UserLinkServiceTest {

    @InjectMocks
    UserLinkService userLinkService;

    @Mock
    UserLinkRepository userLinkRepository;

    @Spy
    UserLinkMapper userLinkMapper;

    @Test
    void 유저링크변경_유저링크변경성공() {
        //given
        doNothing().when(userLinkRepository).deleteAllByCreatedBy(anyLong());
        doReturn(UserLinkFixture.createUserLinkFixture()).when(userLinkRepository).saveAll(Mockito.<UserLink>anyList());

        //when
        List<UserLink> userLinks = userLinkService.changeUserLinks(1L, UserLinkFixture.createUserLinkDtoFixture());

        //then
        assertThat(userLinks).extracting("url").containsExactly("링크1", "링크2");
    }
}