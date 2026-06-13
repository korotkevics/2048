package ch.korotkevics.play2048.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataGameHistoryRepository extends JpaRepository<GameHistoryEntity, Long> {
    
    List<GameHistoryEntity> findByClientIdOrderByCreatedAtDesc(String clientId);
    
    Optional<GameHistoryEntity> findFirstByClientIdOrderByCreatedAtDesc(String clientId);
    
    @Modifying
    @Query("DELETE FROM GameHistoryEntity h WHERE h.createdAt < :threshold")
    void deleteByCreatedAtBefore(Instant threshold);

    @Modifying
    @Query("DELETE FROM GameHistoryEntity h WHERE h.clientId = :clientId")
    void deleteByClientId(String clientId);
}
