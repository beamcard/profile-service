package com.beamcard.profile.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.exception.LinkNotFoundException;
import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.LinkRepository;
import com.beamcard.profile.domain.service.LinkService.CreateLinkCommand;
import com.beamcard.profile.domain.service.LinkService.UpdateLinkCommand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceImplTest {

    @Mock
    LinkRepository linkRepository;

    @Mock
    ProfileService profileService;

    LinkServiceImpl service;

    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        service = new LinkServiceImpl(linkRepository, profileService);
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        Profile profile =
                Profile.builder().id(profileId).userId(userId).username("alice").build();
        // create provisions; edits resolve an existing profile — stub both, lenient per test.
        lenient().when(profileService.getOrProvision(userId, "alice")).thenReturn(profile);
        lenient().when(profileService.getByUserId(userId)).thenReturn(profile);
    }

    private Link link(UUID id, int position) {
        return Link.builder()
                .id(id)
                .profileId(profileId)
                .label("L" + position)
                .url("https://example.com/" + position)
                .type(LinkType.GENERIC)
                .position(position)
                .build();
    }

    @Test
    void create_appendsAtEnd_withNextPosition() {
        when(linkRepository.findByProfileId(profileId)).thenReturn(List.of(link(UUID.randomUUID(), 1)));
        when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(userId, "alice", new CreateLinkCommand("Site", "https://site.com", LinkType.GENERIC));

        ArgumentCaptor<Link> captor = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(captor.capture());
        assertThat(captor.getValue().getPosition()).isEqualTo(2); // 1-based: count(1) + 1
        assertThat(captor.getValue().getProfileId()).isEqualTo(profileId);
    }

    @Test
    void create_defaultsTypeToGeneric_whenNull() {
        when(linkRepository.findByProfileId(profileId)).thenReturn(List.of());
        when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(userId, "alice", new CreateLinkCommand("Site", "https://site.com", null));

        ArgumentCaptor<Link> captor = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(LinkType.GENERIC);
    }

    @Test
    void update_appliesOnlyNonNullFields() {
        UUID linkId = UUID.randomUUID();
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(link(linkId, 0)));
        when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Link result = service.update(userId, linkId, new UpdateLinkCommand("New label", null, null));

        assertThat(result.getLabel()).isEqualTo("New label");
        assertThat(result.getUrl()).isEqualTo("https://example.com/0"); // unchanged
    }

    @Test
    void update_throws_whenLinkOwnedByAnotherProfile() {
        UUID linkId = UUID.randomUUID();
        Link foreign = Link.builder()
                .id(linkId)
                .profileId(UUID.randomUUID()) // different profile
                .label("x")
                .url("https://x.com")
                .type(LinkType.GENERIC)
                .build();
        when(linkRepository.findById(linkId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.update(userId, linkId, new UpdateLinkCommand("h", null, null)))
                .isInstanceOf(LinkNotFoundException.class);
    }

    @Test
    void delete_recompactsRemainingPositions() {
        UUID keep = UUID.randomUUID();
        UUID gone = UUID.randomUUID();
        when(linkRepository.findById(gone)).thenReturn(Optional.of(link(gone, 1)));
        // After deletion the repo returns the survivor still at its old position (2).
        when(linkRepository.findByProfileId(profileId)).thenReturn(List.of(link(keep, 2)));
        when(linkRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.delete(userId, gone);

        verify(linkRepository).deleteById(gone);
        ArgumentCaptor<List<Link>> captor = ArgumentCaptor.forClass(List.class);
        verify(linkRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(l -> assertThat(l.getPosition())
                .isEqualTo(1)); // recompacted to 1-based
    }

    @Test
    void reorder_assignsPositionsByGivenOrder() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(linkRepository.findByProfileId(profileId)).thenReturn(List.of(link(a, 1), link(b, 2)));
        when(linkRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Link> result = service.reorder(userId, List.of(b, a));

        assertThat(result).extracting(Link::getId).containsExactly(b, a);
        assertThat(result).extracting(Link::getPosition).containsExactly(1, 2); // 1-based
    }

    @Test
    void reorder_throws_whenIdNotOwned() {
        UUID a = UUID.randomUUID();
        when(linkRepository.findByProfileId(profileId)).thenReturn(List.of(link(a, 0)));

        assertThatThrownBy(() -> service.reorder(userId, List.of(UUID.randomUUID())))
                .isInstanceOf(LinkNotFoundException.class);
    }
}
