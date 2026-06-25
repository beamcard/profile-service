package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.exception.LinkNotFoundException;
import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.domain.repository.LinkRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final ProfileService profileService;

    @Override
    @Transactional(readOnly = true)
    public List<Link> listByProfileId(UUID profileId) {
        return linkRepository.findByProfileId(profileId);
    }

    @Override
    @Transactional
    public Link create(UUID userId, String username, CreateLinkCommand command) {
        UUID profileId = getProfileId(userId, username);
        int position = linkRepository.findByProfileId(profileId).size() + 1;
        Link created = linkRepository.save(Link.builder()
                .profileId(profileId)
                .label(command.label())
                .url(command.url())
                .type(command.type() == null ? LinkType.GENERIC : command.type())
                .position(position)
                .build());
        log.debug("Added link {} to profile {} at position {}", created.getId(), profileId, position);
        return created;
    }

    @Override
    @Transactional
    public Link update(UUID userId, UUID linkId, UpdateLinkCommand command) {
        UUID profileId = existingProfileId(userId);
        Link current = getOwnedLink(profileId, linkId);

        Link.LinkBuilder builder = current.toBuilder();
        if (command.label() != null) {
            builder.label(command.label());
        }
        if (command.url() != null) {
            builder.url(command.url());
        }
        if (command.type() != null) {
            builder.type(command.type());
        }
        return linkRepository.save(builder.build());
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID linkId) {
        UUID profileId = existingProfileId(userId);
        getOwnedLink(profileId, linkId);
        linkRepository.deleteById(linkId);
        linkRepository.saveAll(recompact(linkRepository.findByProfileId(profileId)));
    }

    @Override
    @Transactional
    public List<Link> reorder(UUID userId, List<UUID> orderedIds) {
        UUID profileId = existingProfileId(userId);
        Map<UUID, Link> byId = new LinkedHashMap<>();
        for (Link link : linkRepository.findByProfileId(profileId)) {
            byId.put(link.getId(), link);
        }

        List<Link> result = new ArrayList<>(byId.size());
        int position = 1;
        for (UUID id : orderedIds) {
            Link link = byId.remove(id);
            if (Objects.isNull(link)) {
                throw new LinkNotFoundException(id);
            }
            result.add(link.toBuilder().position(position++).build());
        }
        for (Link link : byId.values()) {
            result.add(link.toBuilder().position(position++).build());
        }
        return linkRepository.saveAll(result);
    }

    private UUID getProfileId(UUID userId, String username) {
        return profileService.getOrProvision(userId, username).getId();
    }

    private UUID existingProfileId(UUID userId) {
        return profileService.getByUserId(userId).getId();
    }

    private Link getOwnedLink(UUID profileId, UUID linkId) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new LinkNotFoundException(linkId));
        if (!profileId.equals(link.getProfileId())) {
            throw new LinkNotFoundException(linkId);
        }
        return link;
    }

    private static List<Link> recompact(List<Link> ordered) {
        List<Link> recompactedLinks = new ArrayList<>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            int position = i + 1;
            Link link = ordered.get(i);
            recompactedLinks.add(
                    link.getPosition() == position
                            ? link
                            : link.toBuilder().position(position).build());
        }
        return recompactedLinks;
    }
}
