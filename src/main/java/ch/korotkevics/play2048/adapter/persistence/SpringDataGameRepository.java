package ch.korotkevics.play2048.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SpringDataGameRepository extends JpaRepository<GameEntity, String> {
    
    @Modifying
    @Query("DELETE FROM GameEntity g WHERE g.lastActivityAt < :threshold")
    void deleteByLastActivityAtBefore(Instant threshold);
}
