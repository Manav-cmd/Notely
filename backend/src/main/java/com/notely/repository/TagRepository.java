package com.notely.repository;

import com.notely.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);

    @Query("SELECT t.name, COUNT(n) as noteCount FROM Note n JOIN n.tags t GROUP BY t.name ORDER BY noteCount DESC")
    List<Object[]> findMostUsedTags();
}
